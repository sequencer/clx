package linktraining

import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.stage.ChiselStage
import chisel3.util.{Cat, is, switch}

object LinkTrainerUp {
    object State extends ChiselEnum {
        val U1, U2, L0 = Value
    }

    object ts1SetState extends ChiselEnum {
        val S0, S1, S2, S3, S4 = Value
    }
}

class LinkTrainerUp extends Module {
    import LinkTrainerUp.State._
    import LinkTrainerUp.ts1SetState._

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

    val state = RegInit(U1)
    val recvedEightTs1 = RegInit(false.B)
    val oneSetSent = WireInit(false.B)
    val sentEightTs2 = WireInit(false.B)

    io.linkedUp := (state === L0)
    io.txDataOut := WireDefault(0.U(18.W))

    // Upstream link trainer state transition
    switch (state) {
        is (U1) {
            when (oneSetSent && recvedEightTs1) {
                state := U2
            }
        }
        is (U2) {
            when (sentEightTs2) {
                state := L0
            }
        }
    }

    // TS1 recognizer state transition
    val ts1Cnt = RegInit(0.U(3.W))
    val ts1State = RegInit(S0)

    switch (ts1State) {
        is (S0) {
            when (io.rxDataIn === TS1(0)) {
                ts1State := S1
            } .otherwise {
                ts1State := S0
            }
        }
        is (S1) {
            when (io.rxDataIn === TS1(1)) {
                ts1State := S2
            } .otherwise(ts1State := S0)
        }
        is(S2) {
            when (io.rxDataIn === TS1(2)) {
                ts1State := S3
            } .otherwise {
                ts1State := S0
            }
        }
        is(S3) {
            when (io.rxDataIn === TS1(3)) {
                ts1State := S4
                ts1Cnt := ts1Cnt + 1.U
            } .otherwise {
                ts1State := S0
            }
        }
        is(S4) {
            when (io.rxDataIn === TS1(0)) {
                ts1State := S1
            } .otherwise {
                ts1State := S0
                ts1Cnt := 0.U // make sure CONSECUTIVE 8 TS1
            }
        }
    }

//    recvedEightTs1 := (ts1Cnt === 7.U) && (ts1State === S3) && (io.rxDataIn === TS1(3))
    when (recvedEightTs1) {
        recvedEightTs1 := 1.U //
    } .elsewhen ((ts1Cnt === 7.U) && (ts1State === S3) && (io.rxDataIn === TS1(3))) {
        recvedEightTs1 := 1.U
    } .otherwise {
        recvedEightTs1 := recvedEightTs1
    }

    // send TS1 continuously @ U1
    val ts1SetInnerCnt = RegInit(0.U(2.W))
    oneSetSent := ts1SetInnerCnt === 3.U
    
    when (state === U1) {
        ts1SetInnerCnt := ts1SetInnerCnt + 1.U
        switch (ts1SetInnerCnt) {
            is (0.U) {
                io.txDataOut := TS1(0)
            }
            is (1.U) {
                io.txDataOut := TS1(1)
            }
            is (2.U) {
                io.txDataOut := TS1(2)
            }
            is (3.U) {
                io.txDataOut := TS1(3)
            }
        }
    }

    // send 8 TS2 @ U2
    val ts2SetInnerCnt = RegInit(0.U(2.W))
    val ts2SetSentCnt = RegInit(0.U(3.W))
    when (state === U2) {
        ts2SetInnerCnt := ts2SetInnerCnt + 1.U
        switch (ts2SetInnerCnt) {
            is (0.U) {
                io.txDataOut := TS2(0)
            }
            is (1.U) {
                io.txDataOut := TS2(1)
            }
            is (2.U) {
                io.txDataOut := TS2(2)
            }
            is (3.U) {
                io.txDataOut := TS2(3)
                ts2SetSentCnt := ts2SetSentCnt + 1.U
            }
        }
    }

    sentEightTs2 := ts2SetSentCnt === 7.U && ts2SetInnerCnt === 3.U

    when (state === L0) {
        io.txDataOut := 0.U(18.W)
    }
}

object LinkTrainerUpV extends App {
    (new ChiselStage).emitVerilog(new LinkTrainerUp)
}