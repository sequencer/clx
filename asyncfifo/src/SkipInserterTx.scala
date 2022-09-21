package asyncfifo

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._

// clk: txPcsClk
// Insert clk compensation {SKP, COM}
class SkipInserterTx extends Module {
    val io = IO(new Bundle {
        // reg
        val skipIntervalLimit   = Input(UInt(12.W))
        // from upstream
        val txDataIn            = Flipped(Decoupled(UInt(18.W)))
        // to downstream (cannot be flow controlled)
        val txDataOut           = Output(UInt(18.W))
    })

    /** @val symbolCntr unit is TWO symbol */
    val symbolCntr = RegInit(0.U(12.W))
    when (io.txDataIn.fire) {
        symbolCntr := symbolCntr + 1.U // only increment when continuous fire
    } .otherwise {
        symbolCntr := 0.U // otherwise restart the cntr
    }

    io.txDataIn.ready := symbolCntr < io.skipIntervalLimit

    io.txDataOut := WireDefault(Cat("b11".U, CtrlCharacterTable.SKP, CtrlCharacterTable.COM))
    when (io.txDataIn.fire) {
        io.txDataOut := io.txDataIn.bits
    }
}

object SkipInserterTxV extends App {
    (new ChiselStage).emitVerilog(new SkipInserterTx)
}