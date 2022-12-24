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
import resetsync._


class ClxTop extends RawModule {
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

        // add for fakeUpstream
        val clk125M = Output(Clock())
        val linkedUp = Output(Bool())
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
    io.clk125M        := gth.io.clk125M

    resetSynchronizer.io.pllLocked := gth.io.pllLocked
    resetSynchronizer.io.txPcsClk := gth.io.txPcsClk
    resetSynchronizer.io.rxPcsClk := gth.io.rxPcsClk
    resetSynchronizer.io.clk125M := gth.io.clk125M
    resetSynchronizer.io.gtrxreset := gth.io.gtrxreset

    // 125M
    val clxDlClk125M = gth.io.clk125M
    val c2b32b125M = Wire(ValidIO(UInt(p.dataBits.W)))
    val b2c32b125M = Wire(Flipped(ValidIO(UInt(p.dataBits.W))))
    val linkedUp250M = Wire(Bool())

    withClockAndReset (clxDlClk125M, resetSynchronizer.io.clxDlReset.asBool) {
        val clxDataLayer = Module(new CLXDataLayer()(p))

        c2b32b125M <> clxDataLayer.io.c2b
        b2c32b125M <> clxDataLayer.io.b2c
        clxDataLayer.io.tlSlave <> io.tlSlave
        clxDataLayer.io.tlMaster <> io.tlMaster

        val linkedUpPre1 = RegNext(linkedUp250M)
        io.linkedUp := RegNext(linkedUpPre1)
    }

    // 250M
    val txPcsClk = gth.io.txPcsClk
    val ebWr = Wire(Flipped(ValidIO(UInt(18.W))))
    val ebRd = Wire(Flipped(ValidIO(UInt(18.W))))

    withClockAndReset (txPcsClk, resetSynchronizer.io.txReset.asBool) {
        val linkTrainer = Module(new LinkTrainer)
        val txMux = Module(new TxMux3x1)
        val rxMux = Module(new RxMux2x1)
        val encoder = Module(new Encoder)
        val widthAdapterRx = Module(new WidthAdapterRx)
        val WidthAdapterTx = Module(new WidthAdapterTx)

        linkTrainer.io.vioReset := resetSynchronizer.io.linkTrainerReset

        linkTrainer.io.rxDataIn := rxMux.io.linkTrainerRxData

        WidthAdapterTx.io.c2b32bIn := c2b32b125M
        txMux.io.c2b := WidthAdapterTx.io.c2b16bOut
        txMux.io.linkedUp := linkTrainer.io.linkedUp
        txMux.io.ltssmTxData := linkTrainer.io.txDataOut
        resetSynchronizer.io.linkedUp := linkTrainer.io.linkedUp

        encoder.io.txData18b := txMux.io.txData18b
        gth.io.txUserDataIn := encoder.io.encoded20b

        rxMux.io.rxData18b <> ebRd
        rxMux.io.b2c16b <> widthAdapterRx.io.b2c16b
        rxMux.io.linkedUp <> linkTrainer.io.linkedUp

        linkedUp250M := linkTrainer.io.linkedUp

        b2c32b125M <> widthAdapterRx.io.b2c32b
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

object ClxTopV extends App {
    (new chisel3.stage.ChiselStage).emitVerilog(new ClxTop)
}