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


class CLXTopDn extends RawModule {
    val p = CLXLiteParameters()

    val io = IO(new Bundle {
//        val sysReset = Input(Bool())
        // TODO: reset synchronizer
        val txReset    = Input(Bool())    // gth.io.txPcsClk
        val clxDlReset = Input(Bool()) // gth.io.clxDlClk125M
        val rxReset    = Input(Bool())   // gth.io.rxPcsClk

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

        val hbGtwizResetAll = Input(Bool())
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
    gth.io.hbGtwizResetAll := io.hbGtwizResetAll

    // 125M
    val clxDlClk125M = gth.io.clk125M
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

    withClockAndReset (txPcsClk, io.txReset.asBool) {
        val linkTrainerDn = Module(new LinkTrainerDn)
        val txMux = Module(new TxMux3x1)
        val rxMux = Module(new RxMux2x1)
        val encoder = Module(new Encoder)

        linkTrainerDn.io.rxDataIn := rxMux.io.linkTrainerRxData

        txMux.io.c2b := c2b
        txMux.io.linkedUp := linkTrainerDn.io.linkedUp
        txMux.io.ltssmTxData := linkTrainerDn.io.txDataOut

        encoder.io.txData18b := txMux.io.txData18b
        gth.io.txUserDataIn := encoder.io.encoded20b

        rxMux.io.rxData18b <> ebRd
        rxMux.io.b2c <> b2c
        rxMux.io.linkedUp <> linkTrainerDn.io.linkedUp
    }

    // crossing clock domain between 250M RX and 250M TX
    val rxPcsClk = gth.io.rxPcsClk
    val elasticBufferRx = Module(new ElasticBufferRx)

    elasticBufferRx.io.clkWr := rxPcsClk
    elasticBufferRx.io.resetWr := io.rxReset

    elasticBufferRx.io.clkRd := txPcsClk
    elasticBufferRx.io.resetRd := io.txReset

    elasticBufferRx.io.wr <> ebWr
    elasticBufferRx.io.rd <> ebRd

    // 250M RX
    withClockAndReset (rxPcsClk, io.rxReset.asBool) {
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