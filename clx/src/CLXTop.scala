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


class CLXTopUp extends RawModule {
    val p = CLXLiteParameters()

    val io = IO(new Bundle {
//        val sysReset = Input(Bool())
        // TODO: reset synchronizer
        val txPcsReset = Input(Bool()) // gth.io.txPcsClk
        val clxDlReset = Input(Bool()) // gth.io.clxDlClk125M
        val rxPcsReset  = Input(Bool()) // gth.io.rxPcsClk

        // CLXDataLayer
        val tlSlave = Flipped(p.tl.bundle())
        val tlMaster = p.tl.bundle()

        // gthBlackBox
        val pllRefClkP = Input(Bool())
        val pllRefClkN = Input(Bool())
        val txReset = Input(Bool())
        val rxReset = Input(Bool())
        val rxRfInP = Input(UInt(1.W))
        val rXRfInN = Input(UInt(1.W))

        val powerGood = Output(Bool())
        val txRfOutP = Output(UInt(1.W))
        val txRfOutN = Output(UInt(1.W))
    })

    val gth = Module(new GTHBlackBox)
    gth.io.pllRefClkP := io.pllRefClkP
    gth.io.pllRefClkN := io.pllRefClkN
    gth.io.txReset    := io.txReset
    gth.io.rxReset    := io.rxReset
    gth.io.rxRfInP    := io.rxRfInP
    gth.io.rXRfInN    := io.rXRfInN
    io.powerGood      := gth.io.powerGood
    io.txRfOutP       := gth.io.txRfOutP
    io.txRfOutN       := gth.io.txRfOutN

    // 125M
    val clxDlClk125M = gth.io.clxDlClk125M
    val c2b = Wire(ValidIO(UInt(p.dataBits.W)))
    val b2c = Wire(Flipped(ValidIO(UInt(p.dataBits.W))))

    withClockAndReset (clxDlClk125M, io.clxDlReset.asBool) {
        val clxDataLayer = Module(new CLXDataLayer()(p))

        c2b <> clxDataLayer.io.c2b
        b2c <> clxDataLayer.io.b2c
        clxDataLayer.io.tlSlave <> io.tlSlave
        clxDataLayer.io.tlMaster <> io.tlMaster
    }

    // 250M
    val txPcsClk = gth.io.txPcsClk
    val ebWr = Wire(Flipped(ValidIO(UInt(18.W))))
    val ebRd = Wire(Flipped(ValidIO(UInt(18.W))))

    withClockAndReset (txPcsClk, io.txPcsReset.asBool) {
        val linkTrainerUp = Module(new LinkTrainerUp)
        val txMux = Module(new TxMux3x1)
        val rxMux = Module(new RxMux2x1)
        val encoder = Module(new Encoder)

        linkTrainerUp.io.rxDataIn := rxMux.io.linkTrainerRxData

        txMux.io.c2b := c2b
        txMux.io.linkedUp := linkTrainerUp.io.linkedUp
        txMux.io.ltssmTxData := linkTrainerUp.io.txDataOut

        encoder.io.txData18b := txMux.io.txData18b
        gth.io.txUserDataIn := encoder.io.encoded20b

        rxMux.io.rxData18b <> ebRd
        rxMux.io.b2c <> b2c
        rxMux.io.linkedUp <> linkTrainerUp.io.linkedUp
    }

    // crossing clock domain between 250M RX and 250M TX
    val rxPcsClk = gth.io.rxPcsClk
    val elasticBufferRx = Module(new ElasticBufferRx)

    elasticBufferRx.io.clkWr := rxPcsClk
    elasticBufferRx.io.resetWr := io.rxPcsReset

    elasticBufferRx.io.clkRd := txPcsClk
    elasticBufferRx.io.resetRd := io.txPcsReset

    elasticBufferRx.io.wr <> ebWr
    elasticBufferRx.io.rd <> ebRd

    // 250M RX
    withClockAndReset (rxPcsClk, io.rxPcsReset.asBool) {
        val commaDetector = Module(new CommaDetector)
        val decoder = Module(new Decoder)

        commaDetector.io.rxDataIn := gth.io.rxUserDataOut

        decoder.io.symLocked := commaDetector.io.symLocked
        decoder.io.rxData20b := commaDetector.io.rxAligned

        ebWr <> decoder.io.decoded18b
    }
}

object CLXTopV extends App {
    (new chisel3.stage.ChiselStage).emitVerilog(new CLXTopUp)
}