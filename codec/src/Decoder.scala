package codec

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._

// Combination output
// Output the decoded18b at the same cycle of rxData20b
class Decoder extends Module {
    val io = IO(new Bundle {
        val symLocked = Input(Bool())
        val rxData20b = Input(UInt(20.W)) // valid := commaDetector.symLocked
        val decoded18b = ValidIO(UInt(18.W))
        val decoderError = Output(Bool())
    })

    val decoderLow  = Module(new Decoder8b10b)
    val decoderHigh = Module(new Decoder8b10b)

    decoderLow.io.encoded10bInput := io.rxData20b(9, 0)
    decoderHigh.io.encoded10bInput := io.rxData20b(19, 10)

    io.decoded18b.valid := (!io.decoderError) && io.symLocked
    io.decoderError := decoderLow.io.decoderError || decoderHigh.io.decoderError

    io.decoded18b.bits := Cat(decoderHigh.io.controlCharacterValid,
                              decoderLow.io.controlCharacterValid,
                              decoderHigh.io.decoded8bOutput,
                              decoderLow.io.decoded8bOutput)
}

object DecoderV extends App {
    (new ChiselStage).emitVerilog(new Decoder)
}