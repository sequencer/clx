package resetsync

import chisel3._
import chisel3.util._

class ResetSynchronizerBB extends BlackBox {
    val io = IO(new Bundle {
        // I
        val pllLocked = Input(Bool())
        val linkedUp = Input(Bool())
        val txPcsClk = Input(Clock())
        val rxPcsClk = Input(Clock())
        val clk125M = Input(Clock())
        // O
        val txReset = Output(Bool())
        val rxReset = Output(Bool())
        val clxDlReset = Output(Bool())
    })

}