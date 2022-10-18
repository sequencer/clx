package codec

import chisel3._
import chisel3.util._

class Encoder8b10b extends Module {
    val io = IO(new Bundle {
        val isControlCharacter = Input(Bool())
        val decoded8bInput = Input(UInt(8.W))
        val encoded10bOutput = Output(UInt(10.W))
        val runningDisparity = Output(Bool()) // FIXME: useless?
    })

    private def formatLookupVector(mappingSeq: Seq[String]): Vec[UInt] = {
        VecInit(mappingSeq
          .map(_.split(" +"))
          .map(vec => vec.map(element => element.trim))
          .map(vec => vec.size match {
              case 3 => Seq(vec(2), vec(2), vec(2), vec(2))
              case 4 => Seq(vec(2), vec(3), vec(2), vec(3))
              case 6 => Seq(vec(2), vec(3), vec(4), vec(5))
          })
          .map(vec => vec.map(element => ("b" + element).U))
          .flatten,
        )
    }

    private val lookupVec5b6bData = formatLookupVector(EncodeTable.mapping5b6bData)
    private val lookupVec3b4bData = formatLookupVector(EncodeTable.mapping3b4bData)
    private val lookupVec5b6bCtrl = formatLookupVector(EncodeTable.mapping5b6bCtrl)
    private val lookupVec3b4bCtrl = formatLookupVector(EncodeTable.mapping3b4bCtrl)

    // return the encoded bits according to rd(running disparity)
    private def lookup(table: Vec[UInt], data: UInt, rd: Bool, nextGroup: Bool) = {
        table(data * EncodeTable.wordWidth +
              rd.asUInt +
              nextGroup.asUInt * 2.U)
    }

    private def isDisparityNeutral(encodedData: UInt) = {
        // TODO: add to EncodeTable
        PopCount(encodedData) === PopCount(~encodedData)
    }

    // choose group for D.x.7, avoiding continuous 1 or 0
    // TODO: or just two tables as spec ??
    private def shouldUseNextMappingGroup(decodedData5b: UInt, rd: Bool) = {
        val nextGroup = WireInit(false.B)

        when (rd === true.B) {
            when (decodedData5b === 11.U || decodedData5b === 13.U || decodedData5b === 14.U) {
                nextGroup := true.B
            }
        } .otherwise {
            when(decodedData5b === 17.U || decodedData5b === 18.U || decodedData5b === 20.U) {
                nextGroup := true.B
            }
        }
        nextGroup
    }

    // false means negative disparity, true means positive
    val rd = RegInit(false.B)

    val EDCBA = WireInit(io.decoded8bInput(4, 0))
    val HGF = WireInit(io.decoded8bInput(7, 5))

    val abcdei = Wire(UInt(6.W))
    val fghj = Wire(UInt(4.W))
    val rdAfter5b6b = WireInit(true.B)

    when (!io.isControlCharacter) {  // data character
        abcdei := lookup(lookupVec5b6bData, EDCBA, rd, false.B)
        rdAfter5b6b := Mux(isDisparityNeutral(abcdei), rd, !rd)
        fghj := lookup(lookupVec3b4bData, HGF, rdAfter5b6b, shouldUseNextMappingGroup(EDCBA, rdAfter5b6b))
    } .otherwise { // control character
        abcdei := lookup(lookupVec5b6bCtrl, 0.U(5.W), rd, false.B)
        rdAfter5b6b := Mux(isDisparityNeutral(abcdei), rd, !rd)
        fghj := lookup(lookupVec3b4bCtrl, HGF, rdAfter5b6b, false.B)
    }

    val encoded10bReg = RegInit(0.U(10.W))
    encoded10bReg := Cat(abcdei, fghj)
    io.encoded10bOutput := Reverse(encoded10bReg)

    rd := rd ^ (! (isDisparityNeutral(Cat(abcdei, fghj)).asUInt) )
    io.runningDisparity := rd
}

object Encoder8b10bVerilog extends App {
    (new chisel3.stage.ChiselStage).emitVerilog(new Encoder8b10b)
}