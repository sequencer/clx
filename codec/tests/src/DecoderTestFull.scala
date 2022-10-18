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
            var rdOfTestbench = false
            var encoded = 0.U(10.W)
            val dataArray = List.range(0, EncodeTestTable.D_x_y.length, 1)
            val ctrlArray = List.range(0, EncodeTestTable.K_28_y.length, 1)

            // data characters
            for (i <- Random.shuffle(dataArray)) {
                if (rdOfTestbench == false) {
                    encoded = EncodeTestTable.D_x_y(i)(1)
                } else {
                    encoded = EncodeTestTable.D_x_y(i)(2)
                }
                c.io.encoded10bInput.poke(encoded)
                c.io.decoded8bOutput.expect(EncodeTestTable.D_x_y(i)(0))
                c.io.controlCharacterValid.expect(false.B)
                c.io.decoderError.expect(false.B)
                c.clock.step()
                if (EncodeTestTable.D_x_y(i)(3).litValue == 1) {
                    rdOfTestbench = !rdOfTestbench
                }
            }

            // control characters
            for (i <- Random.shuffle(ctrlArray)) {
                if (rdOfTestbench == false) {
                    encoded = EncodeTestTable.K_28_y(i)(1)
                } else {
                    encoded = EncodeTestTable.K_28_y(i)(2)
                }
                c.io.encoded10bInput.poke(encoded)
                c.io.decoded8bOutput.expect(EncodeTestTable.K_28_y(i)(0))
                c.io.controlCharacterValid.expect(true.B)
                c.io.decoderError.expect(false.B)
                c.clock.step()
                if (EncodeTestTable.K_28_y(i)(3).litValue == 1) {
                    rdOfTestbench = !rdOfTestbench
                }
            }

            // wrong characters
            // 1. invalid encoded_10b
            c.io.encoded10bInput.poke("b11111_10000".U)
            c.io.decoded8bOutput.expect("h00".U)
            c.io.controlCharacterValid.expect(false.B)
            c.io.decoderError.expect(true.B)
            c.clock.step()

            c.io.encoded10bInput.poke("b00000_00100".U)
            c.io.decoded8bOutput.expect("h00".U)
            c.io.controlCharacterValid.expect(false.B)
            c.io.decoderError.expect(true.B)
            c.clock.step()

            // TODO
            // 2. rd_wrong, e.g.two consecutive rd- characters
        }
    }
}
