package codec

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._

class Decoder8b10b extends Module {
    val io = IO(new Bundle {
        val encoded10bInput = Input(UInt(10.W))
        val decoded8bOutput = Output(UInt(8.W))
        val controlCharacterValid = Output(Bool())
        val decoderError = Output(Bool())
    })

    private def formatReverseLookupVector(mappingSeq: Seq[String]): Vec[UInt] = {
        val len = mappingSeq(0).split(" +")(2).trim.size

        // e.g. 4b3b decode, 4b as key, {valid(1.W), 3b(3.W)} as value
        def getReverseMap(mappingSeqOrg: Seq[String]): Map[Int, UInt] = {
            mappingSeqOrg
              .map(_.split(" +"))
              .map(_.map(e => e.trim))
              .map(vec => vec.size match {
                  case 3 => Seq(Seq(vec(1), vec(2)))
                  case 4 => Seq(Seq(vec(1), vec(2)), Seq(vec(1), vec(3)))
                  case 6 => Seq(Seq(vec(1), vec(2)), Seq(vec(1), vec(3)),
                                Seq(vec(1), vec(4)), Seq(vec(1), vec(5)))
              }).flatten
              .map(pair => Seq(pair(1), "1" + pair(0))) // reverse key and value, add valid bit
              .map(_ match {
                  case Seq(s1, s2) => (Integer.parseInt(s1, 2), ("b" + s2).U)
              }).toMap // (4 -> "b000".U)
        }

        val vector = (0 until (1 << len)).map(
                      i => (getReverseMap(mappingSeq).withDefault(x => 0.U(1.W)))(i))

        VecInit(vector)
    }

    // lookupVec6b5bCtrl is unnecessary since only use k28.y
    val lookupVec6b5bData = formatReverseLookupVector(EncodeTable.mapping5b6bData)
    val lookupVec4b3bData = formatReverseLookupVector(EncodeTable.mapping3b4bData)
    val lookupVec4b3bCtrl = formatReverseLookupVector(EncodeTable.mapping3b4bCtrl)

    // input order: jhgf iedcba
    val abcdei = Reverse(io.encoded10bInput(5, 0))
    val fghj = Reverse(io.encoded10bInput(9, 6))

    val vEDBCA = Wire(UInt(6.W))
    val vHGFData = Wire(UInt(4.W))
    val vHGFCtrl = Wire(UInt(4.W))

    vHGFData := lookupVec4b3bData(fghj)
    vHGFCtrl := WireDefault(0.U(4.W))

    io.controlCharacterValid := WireInit(false.B)

    // "K.28     11100   001111  110000"
    when (abcdei === "b001111".U || abcdei === "b110000".U) {
        vEDBCA := Cat(1.U(1.W), "b11100".U)
        vHGFCtrl := lookupVec4b3bCtrl(fghj)
        when (vHGFCtrl(3)) {
            io.controlCharacterValid := true.B
        }
    } .otherwise {
        vEDBCA := lookupVec6b5bData(abcdei)
    }

    val isDataCharacter = vEDBCA(5) && vHGFData(3)

    // outputs
    when (vEDBCA(5) && (vHGFData(3) || vHGFCtrl(3))) {
        io.decoderError := false.B
    } .otherwise {
        io.decoderError := true.B
    }

    // FIXME
    // isDataCharacter only means SEPERATELY 6b and 4b can be found in table
    // NOT consider rd
    io.decoderError := Mux(io.controlCharacterValid || isDataCharacter, false.B, true.B)

    when (!io.decoderError) {
        io.decoded8bOutput := Mux(!io.controlCharacterValid, Cat(vHGFData(2, 0), vEDBCA(4,0)), Cat(vHGFCtrl(2, 0), vEDBCA(4,0)))
    } .otherwise {
        io.decoded8bOutput := "h00".U
    }
}

object Decoder8b10bVerilog extends App {
    (new ChiselStage).emitVerilog(new Decoder8b10b)
}