package comd

import chisel3._
import chisel3.util._

class CommaDetector extends Module {
    val io = IO(new Bundle {
        val rx_user_data_in = Input(UInt(20.W))
        val symbol_locked = Output(Bool())
        val rx_data_aligned = Output(UInt(20.W))
    })

    // cat two data_20b into data_40b and shift
    val data40b = RegInit(0.U(40.W))
    data40b := (data40b(39, 20)) | Cat(io.rx_user_data_in, 0.U(20.W))
    val data40b_d = RegNext(data40b)

    // output reg definition
    val locked_r = RegInit(false.B)
    io.symbol_locked := locked_r

    // comma detection
    val comn = "b01_0111_1100".U
    val comp = "b10_1000_0011".U
    val offset_idx_r = RegInit(0.U(5.W))

    val match_array = VecInit((0 to 19).map(
                                i => (data40b(i + 9, i) === comn ||
                                data40b(i + 9, i) === comp)))

    // locked symbol
    when (match_array.contains(true.B)) {
        locked_r := true.B
        offset_idx_r := match_array.indexWhere(_ === true.B)
    }

    // combination output data
    when (locked_r) {
        io.rx_data_aligned := (data40b_d >> offset_idx_r) & "hf_ffff".U
    } .otherwise {
        io.rx_data_aligned := 0.U(20.W)
    }
}

object CommaDetectorV extends App {
    (new chisel3.stage.ChiselStage).emitVerilog(new CommaDetector)
}


