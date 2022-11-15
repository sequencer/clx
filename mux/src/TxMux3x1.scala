package mux

import chisel3._
import chisel3.util._

// mux output to Encoder
class TxMux3x1 extends Module {
    val io = IO(new Bundle {
        val c2b = Flipped(ValidIO(UInt(18.W))) // from CLXDataLayer
        val linkedUp = Input(Bool()) // from LinkTrainer
        val ltssmTxData = Input(UInt(18.W)) // LinkTrainer
        val txData18b = Output(UInt(18.W))
    })

    val SKP = "h1c".U(8.W)
    val COM = "hbc".U(8.W)

    when (!io.linkedUp) {
        io.txData18b := io.ltssmTxData
    } .elsewhen (io.c2b.valid) {
        io.txData18b := io.c2b.bits
    } .otherwise {
        io.txData18b := Cat("b11".U(2.W), SKP, COM)
    }
}

object TxMux3x1V extends App {
    (new chisel3.stage.ChiselStage).emitVerilog(new TxMux3x1)
}