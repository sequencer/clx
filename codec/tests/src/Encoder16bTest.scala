package codec

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

import scala.Array._

class Encoder16bTest extends AnyFlatSpec with ChiselScalatestTester{
    behavior of "Encoder"
    it should "encode" in {
        test(new Encoder).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
          c.io.txData18b.poke("h31cbc".U)
            c.clock.step(30)
        }
    }
}
