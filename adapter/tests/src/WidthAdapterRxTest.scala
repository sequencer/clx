package adapter

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class WidthAdapterRxTest extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "WidthAdapterRx"
    it should "pack data16b to data32b" in {
        test(new WidthAdapterRx).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
            c.clock.step(1)
            // {SKP, COM}
            c.io.controlCharacter.poke("b00".U)
            c.io.data16b.poke("h1cbc".U)

            c.clock.step(1)
            c.io.valid32b.expect(0.U)
            c.io.data32b.expect(0.U(32.W))

            // {STP, COM}
            c.io.controlCharacter.poke("b11".U)
            c.io.data16b.poke("h7cbc".U)

            c.clock.step(1)
            c.io.valid32b.expect(0.U)
            c.io.data32b.expect(0.U(32.W))

            // data ping
            c.io.controlCharacter.poke("b00".U)
            c.io.data16b.poke("h0001".U)

            c.clock.step(1)
            c.io.valid32b.expect(0.U)

            c.io.controlCharacter.poke("b00".U)
            c.io.data16b.poke("h0002".U)

            c.clock.step(1)
//            c.io.valid32b.expect(1.U)
//            c.io.data32b.expect("h00020001".U(32.W))

            // data pong
            c.io.controlCharacter.poke("b00".U)
            c.io.data16b.poke("h1001".U)

            c.clock.step(1)
            c.io.valid32b.expect(1.U)

            c.io.controlCharacter.poke("b00".U)
            c.io.data16b.poke("h1002".U)

            c.clock.step(1)
//            c.io.valid32b.expect(1.U)
//            c.io.data32b.expect("h10021001".U(32.W))

            // data ping
            c.io.controlCharacter.poke("b00".U)
            c.io.data16b.poke("h0003".U)

            c.clock.step(1)

            c.io.controlCharacter.poke("b00".U)
            c.io.data16b.poke("h0004".U)

            c.clock.step(1)

            // data pong
            c.io.controlCharacter.poke("b00".U)
            c.io.data16b.poke("h1003".U)

            c.clock.step(1)
            c.io.valid32b.expect(1.U)

            c.io.controlCharacter.poke("b00".U)
            c.io.data16b.poke("h1004".U)

            c.clock.step(1)

            // data ping
            c.io.controlCharacter.poke("b00".U)
            c.io.data16b.poke("h0005".U)

            c.clock.step(1)

            c.io.controlCharacter.poke("b00".U)
            c.io.data16b.poke("h0006".U)

            c.clock.step(1)

            // END
            c.io.controlCharacter.poke("b11".U)
            c.io.data16b.poke("h9cbc".U)
            c.clock.step(2)

        }
    }
}
