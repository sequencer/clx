package comd

import chisel3._
import chisel3.util._

class CommaDetector extends Module {
    val io = IO(new Bundle {
        val rxDataIn = Input(UInt(20.W))
        val symLocked = Output(Bool())
        val rxAligned = Output(UInt(20.W))
    })

    // cat two data20b into data40b by shifting
    val data40b = RegInit(0.U(40.W))
    data40b := (data40b(39, 20)) | Cat(io.rxDataIn, 0.U(20.W))
    val data40b_d = RegNext(data40b)

    // output reg definition
    val lockedReg = RegInit(false.B)
    io.symLocked := lockedReg

    // comma detection
    val comn = "b01_0111_1100".U
    val comp = "b10_1000_0011".U
    val offsetReg = RegInit(0.U(5.W))

    // use 20 comparators
    val match_array = VecInit((0 to 19).map( i =>
                                (data40b(i + 9, i) === comn ||
                                 data40b(i + 9, i) === comp )))

    // symbol locked
    when (match_array.contains(true.B)) { // 20-input OR Gate
        lockedReg := true.B
        offsetReg := match_array.indexWhere(_ === true.B) // nested if-else
    }

    // combination output data
    when (lockedReg) {
        // false path
        // shift dynamically only once
        io.rxAligned := (data40b_d >> offsetReg) & "hf_ffff".U
    } .otherwise {
        io.rxAligned := 0.U(20.W)
    }
}

object CommaDetectorV extends App {
    (new chisel3.stage.ChiselStage).emitVerilog(new CommaDetector)
}


