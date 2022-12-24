package linktraining

import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.stage.ChiselStage
import chisel3.util.{Cat, is, switch}

object RecvTsCntr {
    object ts1State extends ChiselEnum {
        val A0, A1, A2, A3, A4 = Value
    }

    object ts2State extends ChiselEnum {
        val B0, B1, B2, B3, B4 = Value
    }
}

class RecvTsCntr extends Module {

    import RecvTsCntr.ts1State._
    import RecvTsCntr.ts2State._

    val io = IO(new Bundle {
        val clear = Input(Bool())
        val rxDataIn = Input(UInt(18.W))
        val recv8ts1 = Output(Bool())
        val recv8ts2 = Output(Bool())
        val recvFirstTs2 = Output(Bool())
    })

    // COM k28.5 BC;   TS1 D10.2 4A;   TS2 D5.2 45
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

    val ts1State = RegInit(A0)
    val ts1Cnt = RegInit(0.U(3.W))
    val ts2State = RegInit(B0)
    val ts2Cnt = RegInit(0.U(3.W))

    // 1. State Transition
    when(io.clear) {
        ts1State := A0
        ts1Cnt := 0.U
    }.otherwise {
        switch(ts1State) {
            is(A0) {
                when(io.rxDataIn === TS1(0)) {
                    ts1State := A1
                }.otherwise {
                    ts1State := A0
                    ts1Cnt := 0.U
                }
            }
            is(A1) {
                when(io.rxDataIn === TS1(1)) {
                    ts1State := A2
                }.otherwise {
                    ts1State := A0
                    ts1Cnt := 0.U
                }
            }
            is(A2) {
                when(io.rxDataIn === TS1(2)) {
                    ts1State := A3
                }.otherwise {
                    ts1State := A0
                    ts1Cnt := 0.U
                }
            }
            is(A3) {
                when(io.rxDataIn === TS1(3)) {
                    ts1State := A4
                    ts1Cnt := ts1Cnt + 1.U
                }.otherwise {
                    ts1State := A0
                    ts1Cnt := 0.U
                }
            }
            is(A4) {
                when(io.rxDataIn === TS1(0)) {
                    ts1State := A1
                }.otherwise {
                    ts1State := A0
                    ts1Cnt := 0.U // make sure CONSECUTIVE 8 TS1
                }
            }
        }
    }

    val recvFirstTs2Reg = RegInit(false.B)
    io.recvFirstTs2 := recvFirstTs2Reg
    when(io.clear) {
        recvFirstTs2Reg := false.B
    }.elsewhen(ts2Cnt === 1.U) {
        recvFirstTs2Reg := true.B
    }.otherwise {
        recvFirstTs2Reg := recvFirstTs2Reg
    }

    when(io.clear) {
        ts2State := B0
        ts2Cnt := 0.U
    }.otherwise {
        switch(ts2State) {
            is(B0) {
                when(io.rxDataIn === TS2(0)) {
                    ts2State := B1
                }.otherwise {
                    ts2State := B0
                    ts2Cnt := 0.U
                }
            }
            is(B1) {
                when(io.rxDataIn === TS2(1)) {
                    ts2State := B2
                }.otherwise {
                    ts2State := B0
                    ts2Cnt := 0.U
                }
            }
            is(B2) {
                when(io.rxDataIn === TS2(2)) {
                    ts2State := B3
                }.otherwise {
                    ts2State := B0
                    ts2Cnt := 0.U
                }
            }
            is(B3) {
                when(io.rxDataIn === TS2(3)) {
                    ts2State := B4
                    ts2Cnt := ts2Cnt + 1.U
                }.otherwise {
                    ts2State := B0
                    ts2Cnt := 0.U
                }
            }
            is(B4) {
                when(io.rxDataIn === TS2(0)) {
                    ts2State := B1
                }.otherwise {
                    ts2State := B0
                    ts2Cnt := 0.U
                }
            }
        }
    }

    // 2. output judgement
    val recv8ts1Reg = RegInit(false.B)
    io.recv8ts1 := recv8ts1Reg
    when(io.clear) {
        recv8ts1Reg := false.B
    }.elsewhen(recv8ts1Reg) {
        recv8ts1Reg := true.B
    }.elsewhen(ts1Cnt === 7.U && ts1State === A3 && io.rxDataIn === TS1(3)) {
        recv8ts1Reg := true.B
    }.otherwise {
        recv8ts1Reg := false.B
    }

    val recv8ts2Reg = RegInit(false.B)
    io.recv8ts2 := recv8ts2Reg
    when(io.clear) {
        recv8ts2Reg := false.B
    }.elsewhen(recv8ts2Reg) {
        recv8ts2Reg := true.B
    }.elsewhen(ts2Cnt === 7.U && ts2State === B3 && io.rxDataIn === TS2(3)) {
        recv8ts2Reg := true.B
    }.otherwise {
        recv8ts2Reg := false.B
    }
}

object RecvTsCntrV extends App {
    (new ChiselStage).emitVerilog(new RecvTsCntr)
}