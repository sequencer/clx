package pma

import chisel3._

class GTHBlackBox extends BlackBox {
    val io = IO(new Bundle {
        // PCS Part
        val powerGood       = Output(Bool())
        // tx
        val txReset         = Input(Bool())
        val txUserDataIn    = Input(UInt(20.W))
        val txPcsClk        = Output(Clock())

        // rx
        val rxReset         = Input(Bool())
        val rxPcsClk        = Output(Clock())
        val rxUserDataOut   = Output(UInt(20.W))

        // pll
        val pllRefClkP      = Input(Clock())
        val pllRefClkN      = Input(Clock())
        val clxDlClk125M      = Output(Clock())

        // RF
        val txRfOutP        = Output(UInt(1.W))
        val txRfOutN        = Output(UInt(1.W))

        val rxRfInP         = Input(UInt(1.W))
        val rXRfInN         = Input(UInt(1.W))
    })
}