package adapter

import chisel3._
import chisel3.util._

// 250M
// 125M 32b -> 250M 16b
class WidthAdapterTx extends Module {
    val io = IO(new Bundle {
        val c2b32bIn  = Flipped(ValidIO(UInt(32.W)))
        val c2b16bOut = ValidIO(UInt(16.W))
    })

    val dataInReg = RegNext(io.c2b32bIn.bits)
    val dataOutReg = RegNext(dataInReg)

    val validInReg = RegNext(io.c2b32bIn.valid)
    val validOutReg = RegNext(validInReg)

    val sel = Wire(Bool())
    sel := RegEnable(~sel, false.B, validOutReg)

    io.c2b16bOut.valid := validOutReg
    when(validOutReg) {
        when (sel === false.B) {
            io.c2b16bOut.bits := dataOutReg(15, 0)
        } .otherwise {
            io.c2b16bOut.bits := dataOutReg(31, 16)
        }
    } .otherwise {
        io.c2b16bOut.bits := 0.U(16.W)
    }
}
object WidthAdapterTxV extends App {
    (new chisel3.stage.ChiselStage).emitVerilog(new WidthAdapterTx)
}