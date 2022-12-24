package codec

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._

// tx path continuously sends data
// data source: CXLLite, LinkerTrainer or default {SKP, COM}
class Encoder extends Module {
    val io = IO(new Bundle {
        val txData18b = Input(UInt(18.W))
        val encoded20b = Output(UInt(20.W)) // -> pma txUserDataIn
    })

    val encoderLow  = Module(new Encoder8b10b)
    val encoderHigh = Module(new Encoder8b10b)
    val rd = RegInit(false.B)
//    val rd = WireInit(false.B)

    // cascade running disparity
    encoderLow.io.rdInput := rd
    encoderHigh.io.rdInput := encoderLow.io.rdOutput
    rd := encoderHigh.io.rdOutput

    encoderLow.io.isControlCharacter  := io.txData18b(16)
    encoderHigh.io.isControlCharacter := io.txData18b(17)

    encoderLow.io.decoded8bInput  := io.txData18b(7, 0)
    encoderHigh.io.decoded8bInput := io.txData18b(15, 8)

    io.encoded20b := Cat(encoderHigh.io.encoded10bOutput,
                         encoderLow.io.encoded10bOutput)
}

object EncoderV extends App {
    (new ChiselStage).emitVerilog(new Encoder)
}