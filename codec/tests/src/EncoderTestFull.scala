package codec

import chisel3._
import chiseltest._
import scala.util.Random
import Array._
import org.scalatest.flatspec.AnyFlatSpec

class EncoderTestFull extends AnyFlatSpec with ChiselScalatestTester{
    behavior of "Encoder8b10b"
    it should "encode all table entries" in {
        test(new Encoder8b10b).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
            var rd_tb = false
            var encoded = 0.U(10.W)
            val d_array = List.range(0, EncodeTestTable.D_x_y.length, 1)
            val k_array = List.range(0, EncodeTestTable.K_28_y.length, 1)

            c.clock.step()
            for (i <- Random.shuffle(d_array)) {
                c.io.control_i.poke(0.U) // data
                c.io.decoded_8b_i.poke(EncodeTestTable.D_x_y(i)(0))
                if (!c.io.rd_o.peekBoolean()) { //
                    encoded = EncodeTestTable.D_x_y(i)(1)
                } else {
                    encoded = EncodeTestTable.D_x_y(i)(2)
                }

                c.clock.step()
                val flip_bool = (EncodeTestTable.D_x_y(i)(3).litValue == 1)
                if (flip_bool) {
                    rd_tb = !rd_tb // flip
                }
                c.io.rd_o.expect(rd_tb.asBool)
                c.io.encoded_10b_o.expect(encoded)
            }

            for (i <- Random.shuffle(k_array)) {
                c.io.control_i.poke(1.U) // control
                c.io.decoded_8b_i.poke(EncodeTestTable.K_28_y(i)(0))
                if (!c.io.rd_o.peekBoolean()) { //
                    encoded = EncodeTestTable.K_28_y(i)(1)
                } else {
                    encoded = EncodeTestTable.K_28_y(i)(2)
                }

                c.clock.step()
                val flip_bool = (EncodeTestTable.K_28_y(i)(3).litValue == 1)
                if (flip_bool) {
                    rd_tb = !rd_tb // flip
                }
                c.io.rd_o.expect(rd_tb.asBool)
                c.io.encoded_10b_o.expect(encoded)
            }
        }
    }
}
