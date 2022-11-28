package clx

import adapter._
import asyncfifo._
import chisel3._
import chisel3.util._
import clxdl._
import codec._
import comd._
import linktraining._
import mux._
import pma._
import resetsync.ResetSynchronizerBB


class CLXTopDn extends RawModule {
    val p = CLXLiteParameters()

    val io = IO(new Bundle {
        val sysReset = Input(Bool())

        // CLXDataLayer
        val tlSlave = Flipped(p.tl.bundle())
        val tlMaster = p.tl.bundle()

        // gthBlackBox
        val pllRefClkP = Input(Bool())
        val pllRefClkN = Input(Bool())
        val rxRfInP = Input(UInt(1.W))
        val rXRfInN = Input(UInt(1.W))

        val powerGood = Output(Bool())
        val txRfOutP = Output(UInt(1.W))
        val txRfOutN = Output(UInt(1.W))
    })

    val gth = Module(new GTHBlackBox)
    val resetSynchronizer = Module(new ResetSynchronizerBB)

    gth.io.pllRefClkP := io.pllRefClkP
    gth.io.pllRefClkN := io.pllRefClkN
    gth.io.txReset    := resetSynchronizer.io.txReset
    gth.io.rxReset    := resetSynchronizer.io.rxReset
    gth.io.rxRfInP    := io.rxRfInP
    gth.io.rXRfInN    := io.rXRfInN
    io.powerGood      := gth.io.powerGood
    io.txRfOutP       := gth.io.txRfOutP
    io.txRfOutN       := gth.io.txRfOutN
    gth.io.sysReset   := io.sysReset

    resetSynchronizer.io.pllLocked := gth.io.pllLocked
    resetSynchronizer.io.txPcsClk := gth.io.txPcsClk
    resetSynchronizer.io.rxPcsClk := gth.io.rxPcsClk
    resetSynchronizer.io.clk125M := gth.io.clk125M

    // 125M
    val clxDlClk125M = gth.io.clk125M
    val c2b32b125M = Wire(ValidIO(UInt(p.dataBits.W)))
    val b2c32b125M = Wire(Flipped(ValidIO(UInt(p.dataBits.W))))

    withClockAndReset (clxDlClk125M, resetSynchronizer.io.clxDlReset.asBool) {
        val clxDataLayer = Module(new CLXDataLayer()(p))

        c2b32b125M <> clxDataLayer.io.c2b
        b2c32b125M <> clxDataLayer.io.b2c
        clxDataLayer.io.tlSlave <> io.tlSlave
        clxDataLayer.io.tlMaster <> io.tlMaster
    }

    // 250M
    val txPcsClk = gth.io.txPcsClk
    val ebWr = Wire(Flipped(ValidIO(UInt(18.W))))
    val ebRd = Wire(Flipped(ValidIO(UInt(18.W))))
    val c2b18b250M = Wire(ValidIO(UInt(18.W)))
    c2b18b250M.valid := c2b32b125M.valid
    val low16b = WireInit(true.B)
    c2b18b250M.bits := Mux(low16b, Cat(0.U(2.W), c2b32b125M.bits(15, 0)), Cat(0.U(2.W), c2b32b125M.bits(31, 16)))

    withClockAndReset (txPcsClk, resetSynchronizer.io.txReset.asBool) {
        val linkTrainerDn = Module(new LinkTrainerDn)
        val txMux = Module(new TxMux3x1)
        val rxMux = Module(new RxMux2x1)
        val encoder = Module(new Encoder)
        val widthAdapterRx = Module(new WidthAdapterRx)

        linkTrainerDn.io.rxDataIn := rxMux.io.linkTrainerRxData

        txMux.io.c2b := c2b18b250M
        txMux.io.linkedUp := linkTrainerDn.io.linkedUp
        txMux.io.ltssmTxData := linkTrainerDn.io.txDataOut
        resetSynchronizer.io.linkedUp := linkTrainerDn.io.linkedUp

        encoder.io.txData18b := txMux.io.txData18b
        gth.io.txUserDataIn := encoder.io.encoded20b

        rxMux.io.rxData18b <> ebRd
        rxMux.io.b2c16b <> widthAdapterRx.io.b2c16b
        rxMux.io.linkedUp <> linkTrainerDn.io.linkedUp

        b2c32b125M <> widthAdapterRx.io.b2c32b

        val low16bReg = RegInit(true.B)
        low16bReg := !low16bReg
        low16b := low16bReg
    }

    // crossing clock domain between 250M RX and 250M TX
    val rxPcsClk = gth.io.rxPcsClk
    val elasticBufferRx = Module(new ElasticBufferRx)

    elasticBufferRx.io.clkWr := rxPcsClk
    elasticBufferRx.io.resetWr := resetSynchronizer.io.rxReset

    elasticBufferRx.io.clkRd := txPcsClk
    elasticBufferRx.io.resetRd := resetSynchronizer.io.txReset

    elasticBufferRx.io.wr <> ebWr
    elasticBufferRx.io.rd <> ebRd

    // 250M RX
    withClockAndReset (rxPcsClk, resetSynchronizer.io.rxReset.asBool) {
        val commaDetector = Module(new CommaDetector)
        val decoder = Module(new Decoder)

        commaDetector.io.rxDataIn := gth.io.rxUserDataOut

        decoder.io.symLocked := commaDetector.io.symLocked
        decoder.io.rxData20b := commaDetector.io.rxAligned

        ebWr <> decoder.io.decoded18b
    }
}

object CLXTopDnV extends App {
    (new chisel3.stage.ChiselStage).emitVerilog(new CLXTopDn)
}