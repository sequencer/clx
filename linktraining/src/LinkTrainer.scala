package linktraining

import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.stage.ChiselStage
import chisel3.util.{Cat, RegEnable, is, switch}

object LinkTrainer {
    object State extends ChiselEnum {
        val DT, PA, PC, L0 = Value
    }
}

class LinkTrainer extends Module {

    import LinkTrainer.State._

    val io = IO(new Bundle {
        val vioReset = Input(Bool())
        val rxDataIn = Input(UInt(18.W))
        val txDataOut = Output(UInt(18.W))
        val linkedUp = Output(Bool())
    })

    val TS1 = Seq(
        Cat("b01".U(2.W), "h4a".U(8.W), "hbc".U(8.W)),
        Cat("b00".U(2.W), "h4a".U(8.W), "h4a".U(8.W)),
        Cat("b00".U(2.W), "h4a".U(8.W), "h4a".U(8.W)),
        Cat("b00".U(2.W), "h4a".U(8.W), "h4a".U(8.W))
    )
    val TS2 = Seq(
        Cat("b01".U(2.W), "h45".U(8.W), "hbc".U(8.W)),
        Cat("b00".U(2.W), "h45".U(8.W), "h45".U(8.W)),
        Cat("b00".U(2.W), "h45".U(8.W), "h45".U(8.W)),
        Cat("b00".U(2.W), "h45".U(8.W), "h45".U(8.W))
    )

    val stateToChange = RegInit(false.B)

    val recvTsCntr = Module(new RecvTsCntr)
    recvTsCntr.io.rxDataIn := io.rxDataIn
    recvTsCntr.io.clear := stateToChange

    val state = RegInit(DT)

    // 0. txdata
    io.txDataOut := Cat("b11".U(2.W), "h1c".U(8.W), "hbc".U(8.W))

    val ts1SetInnerCntr = RegInit(0.U(2.W))
    when(state === PA) {
        ts1SetInnerCntr := ts1SetInnerCntr + 1.U
        switch(ts1SetInnerCntr) {
            is(0.U) {
                io.txDataOut := TS1(0)
            }
            is(1.U) {
                io.txDataOut := TS1(1)
            }
            is(2.U) {
                io.txDataOut := TS1(2)
            }
            is(3.U) {
                io.txDataOut := TS1(3)
            }
        }
    }.otherwise {
        ts1SetInnerCntr := 0.U(2.W)
    }

    val ts2SetInnerCntr = RegInit(0.U(2.W))
    when(state === PC) {
        ts2SetInnerCntr := ts2SetInnerCntr + 1.U
        switch(ts2SetInnerCntr) {
            is(0.U) {
                io.txDataOut := TS2(0)
            }
            is(1.U) {
                io.txDataOut := TS2(1)
            }
            is(2.U) {
                io.txDataOut := TS2(2)
            }
            is(3.U) {
                io.txDataOut := TS2(3)
            }
        }
    }.otherwise {
        ts2SetInnerCntr := 0.U(2.W)
    }

    val xmitTs1Cntr = Reg(UInt(20.W))
    xmitTs1Cntr := RegEnable(xmitTs1Cntr + 1.U(1.W), 0.U(20.W), ts1SetInnerCntr === 3.U)

    // cnt after receiving the first TS2
    val xmitTs2Cntr = RegInit(0.U(10.W))
    when(state === PC && recvTsCntr.io.recvFirstTs2) {
        xmitTs2Cntr := RegEnable(xmitTs2Cntr + 1.U(1.W), 0.U(20.W), ts2SetInnerCntr === 3.U)
    }.otherwise {
        xmitTs2Cntr := 0.U(10.W)
    }

    val passed12ms = RegInit(false.B)
    val timerCntr = RegInit(0.U(22.W))
    timerCntr := timerCntr + 1.U

    when(passed12ms) {
        passed12ms := true.B
        // todo:
    }.elsewhen(timerCntr === "h3fffff".U) {
//    }.elsewhen(timerCntr === "hff".U) {
        passed12ms := true.B
    }.otherwise {
        passed12ms := false.B
    }

    // 1. State Transition
    stateToChange := false.B
    switch(state) {
        is(DT) {
            when (io.vioReset) {
                state := DT
                stateToChange := true.B
            }.elsewhen(passed12ms) {
                state := PA
                stateToChange := true.B
            }
        }
        is(PA) {
            when(io.vioReset) {
                state := DT
                stateToChange := true.B

            }.elsewhen((recvTsCntr.io.recv8ts1 || recvTsCntr.io.recv8ts2) && (xmitTs1Cntr >= 1024.U)) {
                state := PC
                stateToChange := true.B
            }
        }
        is(PC) {
            when(io.vioReset) {
                state := DT
                stateToChange := true.B

            }.elsewhen(recvTsCntr.io.recv8ts2 && (xmitTs2Cntr >= 16.U)) {
                state := L0
                stateToChange := true.B
            }
        }
        is(L0) {
            when(io.vioReset) {
                state := DT
                stateToChange := true.B
            }
        }
    }

    io.linkedUp := state === L0
}

object LinkTrainerV extends App {
    (new ChiselStage).emitVerilog(new LinkTrainer)
}