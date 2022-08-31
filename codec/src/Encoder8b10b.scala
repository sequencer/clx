package codec

import chisel3._
import chisel3.util._

class Encoder8b10b extends Module {
    val io = IO(new Bundle {
        val control_i = Input(Bool())
        val decoded_8b_i = Input(UInt(8.W))
        val encoded_10b_o = Output(UInt(10.W))
        val rd_o = Output(Bool())
    })

    private def format_lookup_vector(mapping_seq: Seq[String]): Vec[UInt] = {
        VecInit(mapping_seq
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

    private val lookup_5b6b_d = format_lookup_vector(EncodeTable.mapping_5b6b_d)
    private val lookup_3b4b_d = format_lookup_vector(EncodeTable.mapping_3b4b_d)
    private val lookup_5b6b_c = format_lookup_vector(EncodeTable.mapping_5b6b_c)
    private val lookup_3b4b_c = format_lookup_vector(EncodeTable.mapping_3b4b_c)

    // return the encoded bits according to rd(running disparity)
    private def lookup(table: Vec[UInt], data: UInt, rd: Bool, nxt_group: Bool) = {
        table(data * EncodeTable.word_width +
              rd.asUInt +
              nxt_group.asUInt * 2.U)
    }

    private def neutral_disparity(encoded_data: UInt) = {
        // TODO:
        // add to EncodeTable
        PopCount(encoded_data) === PopCount(~encoded_data)
    }

    // choose group for D.x.7, avoiding continuous 1 or 0
    // TODO: or just two tables as spec ??
    private def choose_group(data_5b: UInt, rd: Bool) = {
        val nxt_group = WireInit(false.B)

        when (rd === true.B) {
            when (data_5b === 11.U || data_5b === 13.U || data_5b === 14.U) {
                nxt_group := true.B
            }
        } .otherwise {
            when(data_5b === 17.U || data_5b === 18.U || data_5b === 20.U) {
                nxt_group := true.B
            }
        }
        nxt_group
    }

    // false means negative disparity, true means positive
    val rd = RegInit(false.B)

    val EDCBA = WireInit(io.decoded_8b_i(4, 0))
    val HGF = WireInit(io.decoded_8b_i(7, 5))

    val abcdei = Wire(UInt(6.W))
    val fghj = Wire(UInt(4.W))
    val rd_after_5b6b = WireInit(true.B)

    when (!io.control_i) {  // data character
        abcdei := lookup(lookup_5b6b_d, EDCBA, rd, false.B)
        rd_after_5b6b := Mux(neutral_disparity(abcdei), rd, !rd)
        fghj := lookup(lookup_3b4b_d, HGF, rd_after_5b6b, choose_group(EDCBA, rd_after_5b6b))
    } .otherwise { // control character
        abcdei := lookup(lookup_5b6b_c, 0.U(5.W), rd, false.B)
        rd_after_5b6b := Mux(neutral_disparity(abcdei), rd, !rd)
        fghj := lookup(lookup_3b4b_c, HGF, rd_after_5b6b, false.B)
    }

    val encoded_r = RegInit(0.U(10.W))
    encoded_r := Cat(abcdei, fghj)
    io.encoded_10b_o := Reverse(encoded_r)

    rd := rd ^ (! (neutral_disparity(Cat(abcdei, fghj)).asUInt) )
    io.rd_o := rd

}

    object Encoder8b10bVerilog extends App {
    (new chisel3.stage.ChiselStage).emitVerilog(new Encoder8b10b)
}