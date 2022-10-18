package pma

import chisel3._
import chisel3.util.HasBlackBoxResource

class SerdesTopBlackBox extends BlackBox {
    val io = IO(new Bundle {
        // PCS Part
        // tx
        val txReset         = Input(Bool())
        val txUserDataIn    = Input(UInt(20.W))
        val txPowerUp       = Output(Bool())
        val txPcsClk        = Output(Clock())

        // rx
        val rxReset         = Input(Bool())
        val rxPowerUp       = Output(Bool())
        val rxPhaseLocked   = Output(Bool())
        val rxPcsClk        = Output(Clock())
        val rxUserDataOut   = Output(UInt(20.W))

        // pll
        val pllReset        = Input(Bool())
        val pllPowerUp      = Output(Bool())
        val clk125M         = Output(Clock())
        val clk62_5M        = Output(Clock())
        val clk250M         = Output(Clock())

        /* RF */
        val txRfOutP        = Output(UInt(1.W))
        val txRfOutN        = Output(UInt(1.W))

        val rxRfInP         = Input(UInt(1.W))
        val rXRfInN         = Input(UInt(1.W))

        val pllRefClkP      = Input(Clock())
        val pllRefClkN      = Input(Clock())

        /* Serdes Tuning Part */
        // tx
        val txVoltSwing     = Input(UInt(4.W))
        val txDeemphPreCur  = Input(UInt(5.W))
        val txDeemphPostCur = Input(UInt(5.W))
        val txTermResAdj    = Input(UInt(3.W))

        // rx
        val rxGainCtrl      = Input(UInt(7.W))
        val rxCtleManAdj    = Input(UInt(6.W))
        val rxDfeTapVal     = Input(UInt(50.W))
        val rxAgcSwitch     = Input(UInt(1.W))
        val rxtermDcVoltAdj = Input(UInt(4.W))
        val rxTermResAdj    = Input(UInt(3.W))

        // reserved PVT registers
        val txPvtReg        = Input(UInt(10.W))
        val rxPvtReg        = Input(UInt(10.W))
        val pllPvtReg       = Input(UInt(10.W))
    })
}