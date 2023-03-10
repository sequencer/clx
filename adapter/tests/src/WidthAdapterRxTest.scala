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
            c.io.b2c16b.valid.poke(false.B)
            c.io.b2c16b.bits.poke("h1cbc".U)

            c.clock.step(1)
            c.io.b2c32b.valid.expect(0.U)
            c.io.b2c32b.bits.expect(0.U(32.W))

            // data ping
            c.io.b2c16b.valid.poke(true.B)
            c.io.b2c16b.bits.poke("h0001".U)
            c.clock.step(1)
            c.io.b2c16b.bits.poke("h0002".U)
            c.clock.step(1)

            // data pong
            c.io.b2c16b.bits.poke("h1001".U)
            c.clock.step(1)
            //            c.io.valid32b.expect(1.U)
            c.io.b2c16b.bits.poke("h1002".U)
            c.clock.step(1)

            // data ping
            c.io.b2c16b.bits.poke("h0003".U)
            c.clock.step(1)
            c.io.b2c16b.bits.poke("h0004".U)
            c.clock.step(1)

            // data pong
            c.io.b2c16b.bits.poke("h1003".U)
            c.clock.step(1)
            c.io.b2c16b.bits.poke("h1004".U)
            c.clock.step(1)

            // data ping
            c.io.b2c16b.bits.poke("h0005".U)
            c.clock.step(1)
            c.io.b2c16b.bits.poke("h0006".U)
            c.clock.step(1)

            // uncontinuous
            c.io.b2c16b.bits.poke("h1005".U)
            c.clock.step(1)

            c.io.b2c16b.valid.poke(false.B)
            c.clock.step(1)

            c.io.b2c16b.valid.poke(true.B)
            c.io.b2c16b.bits.poke("h1006".U)
            c.clock.step(1)

            //
            c.io.b2c16b.bits.poke("h0007".U)
            c.clock.step(1)

            c.io.b2c16b.valid.poke(false.B)
            c.clock.step(1)

            c.io.b2c16b.valid.poke(true.B)
            c.io.b2c16b.bits.poke("h0008".U)
            c.clock.step(1)

            //
            c.io.b2c16b.bits.poke("h1007".U)
            c.clock.step(1)

            c.io.b2c16b.valid.poke(false.B)
            c.clock.step(1)

            c.io.b2c16b.valid.poke(true.B)
            c.io.b2c16b.bits.poke("h1008".U)
            c.clock.step(1)

            // END
            c.io.b2c16b.valid.poke(false.B)
            c.io.b2c16b.bits.poke("h9cbc".U)
            c.clock.step(6)
        }
    }
}
