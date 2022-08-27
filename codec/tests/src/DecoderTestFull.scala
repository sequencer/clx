package codec

import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

import scala.Array._
import scala.util.Random

class DecoderTestFull extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "Decoder8b10b"
    it should "decode all possibilities" in {
        test(new Decoder8b10b).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
            var rd_tb = false
            var encoded = 0.U(10.W)
            val d_array = List.range(0, EncodeTestTable.D_x_y.length, 1)
            val k_array = List.range(0, EncodeTestTable.K_28_y.length, 1)

            // data characters
            for (i <- Random.shuffle(d_array)) {
                if (rd_tb == false) {
                    encoded = EncodeTestTable.D_x_y(i)(1)
                } else {
                    encoded = EncodeTestTable.D_x_y(i)(2)
                }
                c.io.encoded_10b_i.poke(encoded)
                c.io.decoded_8b_o.expect(EncodeTestTable.D_x_y(i)(0))
                c.io.control_o.expect(false.B)
                c.io.wrong_o.expect(false.B)
                c.clock.step()
                if (EncodeTestTable.D_x_y(i)(3).litValue == 1) {
                    rd_tb = !rd_tb
                }
            }

            // control characters
            for (i <- Random.shuffle(k_array)) {
                if (rd_tb == false) {
                    encoded = EncodeTestTable.K_28_y(i)(1)
                } else {
                    encoded = EncodeTestTable.K_28_y(i)(2)
                }
                c.io.encoded_10b_i.poke(encoded)
                c.io.decoded_8b_o.expect(EncodeTestTable.K_28_y(i)(0))
                c.io.control_o.expect(true.B)
                c.io.wrong_o.expect(false.B)
                c.clock.step()
                if (EncodeTestTable.K_28_y(i)(3).litValue == 1) {
                    rd_tb = !rd_tb
                }
            }

            // wrong characters
            // 1. invalid encoded_10b
            c.io.encoded_10b_i.poke("b11111_10000".U)
            c.io.decoded_8b_o.expect("h00".U)
            c.io.control_o.expect(false.B)
            c.io.wrong_o.expect(true.B)
            c.clock.step()

            c.io.encoded_10b_i.poke("b00000_00100".U)
            c.io.decoded_8b_o.expect("h00".U)
            c.io.control_o.expect(false.B)
            c.io.wrong_o.expect(true.B)
            c.clock.step()

            // TODO
            // 2. rd_wrong, e.g.two consecutive rd- characters
        }
    }
}
