package linktraining

import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.stage.ChiselStage
import chisel3.util.{Cat, is, switch}

object LinkTrainerDn {
    object State extends ChiselEnum {
        val D1, D2, L0 = Value
    }

    object ts1SetState extends ChiselEnum {
        val S0, S1, S2, S3, S4 = Value
    }

    object ts2SetState extends ChiselEnum {
        val T0, T1, T2, T3, T4 = Value
    }
}
class LinkTrainerDn extends Module {
    import LinkTrainerDn.State._
    import LinkTrainerDn.ts1SetState._
    import LinkTrainerDn.ts2SetState._

    // COM k28.5 BC; TS1 D10.2 4A; TS2 D5.2 45
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

    val io = IO(new Bundle {
        val rxDataIn = Input(UInt(18.W))
        val txDataOut = Output(UInt(18.W))
        val linkedUp = Output(Bool())
    })

    val state = RegInit(D1)
    val recvedEightTs1 = WireInit(false.B)
    val recvedEightTs2 = WireInit(false.B)
    val oneSetSent = WireInit(false.B)

    io.linkedUp := (state === L0)
    io.txDataOut := WireDefault(0.U(18.W))

    // Downstream link trainer state transition
    switch (state) {
        is (D1) {
            when (recvedEightTs1) {
                state := D2
            }
        }
        is (D2) {
            when (oneSetSent && recvedEightTs2) {
                state := L0
            }
        }
    }

    // recvedEightTs1
    val ts1Cnt = RegInit(0.U(3.W))
    val ts1State = RegInit(S0)

    switch(ts1State) {
        is(S0) {
            when(io.rxDataIn === TS1(0)) {
                ts1State := S1
            }.otherwise {
                ts1State := S0
            }
        }
        is(S1) {
            when(io.rxDataIn === TS1(1)) {
                ts1State := S2
            }.otherwise(ts1State := S0)
        }
        is(S2) {
            when(io.rxDataIn === TS1(2)) {
                ts1State := S3
            }.otherwise {
                ts1State := S0
            }
        }
        is(S3) {
            when(io.rxDataIn === TS1(3)) {
                ts1State := S4
                ts1Cnt := ts1Cnt + 1.U
            }.otherwise {
                ts1State := S0
            }
        }
        is(S4) {
            when(io.rxDataIn === TS1(0)) {
                ts1State := S1
            }.otherwise {
                ts1State := S0
                ts1Cnt := 0.U // make sure CONSECUTIVE 8 TS1
            }
        }
    }

    recvedEightTs1 := (ts1Cnt === 7.U) && (ts1State === S3) && (io.rxDataIn === TS1(3))

    // recvedEightTs2
    val ts2Cnt = RegInit(0.U(3.W))
    val ts2State = RegInit(T0)

    switch(ts2State) {
        is(T0) {
            when(io.rxDataIn === TS2(0)) {
                ts2State := T1
            }.otherwise {
                ts2State := T0
            }
        }
        is(T1) {
            when(io.rxDataIn === TS2(1)) {
                ts2State := T2
            }.otherwise(ts2State := T0)
        }
        is(T2) {
            when(io.rxDataIn === TS2(2)) {
                ts2State := T3
            }.otherwise {
                ts2State := T0
            }
        }
        is(T3) {
            when(io.rxDataIn === TS2(3)) {
                ts2State := T4
                ts2Cnt := ts2Cnt + 1.U
            }.otherwise {
                ts2State := T0
            }
        }
        is(T4) {
            when(io.rxDataIn === TS2(0)) {
                ts2State := T1
            }.otherwise {
                ts2State := T0
                ts2Cnt := 0.U // make sure CONSECUTIVE 8 TS2
            }
        }
    }

    recvedEightTs2 := (ts2Cnt === 7.U) && (ts2State === T3) && (io.rxDataIn === TS2(3))

    // send TS1 continuously @ D2
    val ts1SetInnerCnt = RegInit(0.U(2.W))
    oneSetSent := ts1SetInnerCnt === 3.U

    when(state === D2) {
        ts1SetInnerCnt := ts1SetInnerCnt + 1.U
        switch(ts1SetInnerCnt) {
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
    }

    when (state === D1 || state === L0) {
        io.txDataOut := 0.U(18.W)
    }
}

object LinkTrainerDnV extends App {
    (new ChiselStage).emitVerilog(new LinkTrainerDn)
}