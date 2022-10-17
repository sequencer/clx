package adapter

import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._

object WidthAdapterRx {
    object State extends ChiselEnum {
        val IDLE, PING, PONG = Value
    }
}

// clk250M
class WidthAdapterRx extends Module {
    import WidthAdapterRx.State._

    val io = IO(new Bundle {
        val controlCharacter = Input(UInt(2.W))
        val data16b = Input(UInt(16.W))
        val valid32b = Output(Bool())
        val data32b = Output(UInt(32.W))
    })

    val StpCom = Cat("h7c".U(8.W), "hbc".U(8.W))
    val EndCom = Cat("h9c".U(8.W), "hbc".U(8.W))

    val reg32bPing = RegInit(0.U(32.W))
    val reg32bPong = RegInit(0.U(32.W))
    val cnt = RegInit(0.U(2.W))
    val validReg = RegInit(0.U(1.W))

    io.valid32b := validReg

    val state = RegInit(IDLE)
    switch (state) {
        is (IDLE) {
            when(io.controlCharacter === "b11".U && io.data16b === StpCom) {
                state := PING
            }
        }
        is (PING) {
            when (io.controlCharacter === "b11".U && io.data16b === EndCom) {
                state := IDLE
            } .elsewhen (cnt === 2.U) {
                state := PONG
            }
        }
        is (PONG) {
            when(io.controlCharacter === "b11".U && io.data16b === EndCom) {
                state := IDLE
            }.elsewhen(cnt === 2.U) {
                state := PING
            }
        }
    }

    val reg32bOut = RegInit(0.U(32.W))
    io.data32b := reg32bOut
    when (cnt === 2.U) {
        when (state === PING) {
            reg32bOut := reg32bPing
        } .elsewhen (state === PONG) {
            reg32bOut := reg32bPong
        } .otherwise {
            reg32bOut := reg32bOut
        }
    }

    // cnt: 0 1 2 1 2 1 2 1 2 0 0 0
    when (state === PING || state === PONG) {
        cnt := cnt + 1.U
        when (cnt === 2.U) {
            cnt := 1.U
        }
    } .otherwise {
        cnt := 0.U
    }

    when(cnt === 0.U && io.data16b =/= EndCom) {
        reg32bPing := Cat(reg32bPing(31, 16), io.data16b)
    }.elsewhen(cnt === 1.U) {
        when(state === PING) {
            reg32bPing := Cat(io.data16b, reg32bPing(15, 0))
        }.elsewhen(state === PONG) {
            reg32bPong := Cat(io.data16b, reg32bPong(15, 0))
        }
    }.elsewhen(cnt === 2.U) {
        when(state === PING) {
            reg32bPong := Cat(reg32bPong(31, 16), io.data16b)  // maybe weird
        }.elsewhen(state === PONG) {
            reg32bPing := Cat(reg32bPing(31, 16), io.data16b)
        }
    }

    when (io.controlCharacter === "b11".U && io.data16b === EndCom) {
        validReg := 0.U
    } .elsewhen(state === PING && cnt === 1.U) {
        validReg := 1.U
    } .otherwise {
        validReg := validReg
    }
}
object WidthAdapterRxV extends App {
    (new chisel3.stage.ChiselStage).emitVerilog(new WidthAdapterRx)
}