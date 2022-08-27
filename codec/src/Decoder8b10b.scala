package codec

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._

class Decoder8b10b extends Module {
    val io = IO(new Bundle {
        val encoded_10b_i = Input(UInt(10.W))
        val decoded_8b_o = Output(UInt(8.W))
        val control_o = Output(Bool())
        val wrong_o = Output(Bool())
    })

    private def format_reverse_lookup_vector(mapping_seq: Seq[String]): Vec[UInt] = {
        val len = mapping_seq(0).split(" +")(2).trim.size

        // e.g. 4b3b decode, 4b as key, {valid(1.W), 3b(3.W)} as value
        def get_reverse_map(mapping_seq_org: Seq[String]): Map[Int, UInt] = {
            mapping_seq_org
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
                      i => (get_reverse_map(mapping_seq).withDefault(x => 0.U(1.W)))(i))

        VecInit(vector)
    }

    // lookup_6b5b_c is unnecessary since only use k28.y
    val lookup_6b5b_d = format_reverse_lookup_vector(EncodeTable.mapping_5b6b_d)
    val lookup_4b3b_d = format_reverse_lookup_vector(EncodeTable.mapping_3b4b_d)
    val lookup_4b3b_c = format_reverse_lookup_vector(EncodeTable.mapping_3b4b_c)

    // input order: jhgf iedcba
    val abcdei = Reverse(io.encoded_10b_i(5, 0))
    val fghj = Reverse(io.encoded_10b_i(9, 6))

    val vEDBCA = Wire(UInt(6.W))
    val vHGF_d = Wire(UInt(4.W))
    val vHGF_c = Wire(UInt(4.W))

    vHGF_d := lookup_4b3b_d(fghj)
    vHGF_c := WireDefault(0.U(4.W))

    io.control_o := WireInit(false.B)

    // "K.28     11100   001111  110000"
    when (abcdei === "b001111".U || abcdei === "b110000".U) {
        vEDBCA := Cat(1.U(1.W), "b11100".U)
        vHGF_c := lookup_4b3b_c(fghj)
        when (vHGF_c(3)) {
            io.control_o := true.B
        }
    } .otherwise {
        vEDBCA := lookup_6b5b_d(abcdei)
    }

    val data_character = vEDBCA(5) && vHGF_d(3)

    // outputs
    when (vEDBCA(5) && (vHGF_d(3) || vHGF_c(3))) {
        io.wrong_o := false.B
    } .otherwise {
        io.wrong_o := true.B
    }

    // FIXME
    // data_character only means SEPERATELY 6b and 4b can be found in table
    // NOT consider rd
    io.wrong_o := Mux(io.control_o || data_character, false.B, true.B)

    when (!io.wrong_o) {
        io.decoded_8b_o := Mux(!io.control_o, Cat(vHGF_d(2, 0), vEDBCA(4,0)), Cat(vHGF_c(2, 0), vEDBCA(4,0)))
    } .otherwise {
        io.decoded_8b_o := "h00".U
    }
}

object Decoder8b10bVerilog extends App {
    (new ChiselStage).emitVerilog(new Decoder8b10b)
}