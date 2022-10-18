package mux

import chisel3._
import chisel3.util._

// mux input from ElasticBuffer
class RxMux2x1 extends Module {
    val io = IO(new Bundle {
        val rxData18b = Flipped(ValidIO(UInt(18.W)))
        val b2c = ValidIO(UInt(18.W)) // to CLXDataLayer
        val linkedUp = Input(Bool()) // from LinkTrainer
        val linkTrainerRxData = Output(UInt(18.W)) // to LinkTrainer

    })

    io.linkTrainerRxData := WireDefault(0.U(18.W))
    io.b2c.valid := WireDefault(0.U(1.W))
    io.b2c.bits := WireDefault(0.U(18.W))

    when (io.rxData18b.valid) {
        when (!io.linkedUp) {
            io.linkTrainerRxData := io.rxData18b.bits
        } .otherwise {
            io.b2c.valid := 1.U
            io.b2c.bits := io.rxData18b.bits
        }
    }
}

object RxMux2x1V extends App {
    (new chisel3.stage.ChiselStage).emitVerilog(new RxMux2x1)
}