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
        val b2c16b = Flipped(ValidIO(UInt(16.W)))
        val b2c32b = ValidIO(UInt(32.W))
    })

    val reg32bPing = RegInit(0.U(32.W))
    val reg32bPong = RegInit(0.U(32.W))
    val cnt = RegInit(0.U(1.W))
    val validReg = RegInit(0.U(1.W))
    val validEnd = RegInit(0.U(1.W))
    val validEndD1 = RegNext(validEnd)

    io.b2c32b.valid := validReg | validEnd | validEndD1

    val state = RegInit(IDLE)
    switch (state) {
        is (IDLE) {
            when(io.b2c16b.valid) {
                state := PING
            }
        }
        is (PING) {
            when (!io.b2c16b.valid && cnt === 1.U) {
                state := IDLE
            } .elsewhen (cnt === 1.U) {
                state := PONG
            }
        }
        is (PONG) {
            when(!io.b2c16b.valid && cnt === 1.U) {
                state := IDLE
            }.elsewhen(cnt === 1.U) {
                state := PING
            }
        }
    }

    val reg32bOut = RegInit(0.U(32.W))
    io.b2c32b.bits := reg32bOut
    when (cnt === 1.U) {
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
    }

    when (cnt === 0.U) {
        when (state === PING) {
            reg32bPing := Cat(io.b2c16b.bits, reg32bPing(15, 0))
        } .elsewhen (state === PONG) {
            reg32bPong := Cat(io.b2c16b.bits, reg32bPong(15, 0))
        } .elsewhen (state === IDLE && io.b2c16b.valid) {
            reg32bPing := Cat(reg32bPing(31, 16), io.b2c16b.bits)
        }
    } .elsewhen (cnt === 1.U) {
        when (state === PING) {
            reg32bPong := Cat(reg32bPong(31, 16), io.b2c16b.bits) // maybe weird
        }.elsewhen(state === PONG) {
            reg32bPing := Cat(reg32bPing(31, 16), io.b2c16b.bits)
        }
    }

    when (!io.b2c16b.valid && cnt === 1.U) {
        validReg := 0.U
    } .elsewhen(state === PING && cnt === 1.U) {
        validReg := 1.U
    } .otherwise {
        validReg := validReg
    }

    when (cnt === 1.U && !io.b2c16b.valid) {
        validEnd := 1.U
    } .otherwise {
        validEnd := 0.U
    }

}
object WidthAdapterRxV extends App {
    (new chisel3.stage.ChiselStage).emitVerilog(new WidthAdapterRx)
}