package linktraining

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class LinkTrainerTest extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "LinkTrainerConnector"
    it should "test by connecting up and dn" in {
        test(new LinkTrainerConnector).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
            c.clock.step(200)
        }

    }
}
