package comd

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class CommaDetectorTest extends AnyFlatSpec with ChiselScalatestTester {
    behavior of "CommaDetector"
    it should "Detect positive comma" in {
        test(new CommaDetector).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
            c.io.rx_user_data_in.poke("b0001_0111_1100_1100_0001".U)
            c.clock.step(1)
            c.io.rx_user_data_in.poke("b0001_0111_0000_1100_0001".U)
            c.clock.step(1)

            c.io.rx_user_data_in.poke("b1001_0101_0000_1100_0101".U)
            c.clock.step(1)
            c.io.symbol_locked.expect(1.U)
            c.io.rx_data_aligned.expect("b1100_0001_0001_0111_1100".U)

            c.io.rx_user_data_in.poke("b1001_0101_0000_1100_0100".U)
            c.clock.step(1)
            c.io.symbol_locked.expect(1.U)
            c.io.rx_data_aligned.expect("b1100_0101_0001_0111_0000".U)

            c.clock.step(1)
            c.io.symbol_locked.expect(1.U)
            c.io.rx_data_aligned.expect("b1100_0100_1001_0101_0000".U)

            c.clock.step(5)
        }
    }
    it should "Detect negative comma" in {
        test(new CommaDetector).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
            c.io.rx_user_data_in.poke("b1_0010_1000_0011_1100_001".U)
            c.clock.step(1)
            c.io.rx_user_data_in.poke("b0001_0111_0000_1100_0001".U)
            c.clock.step(1)

            c.io.rx_user_data_in.poke("b1001_0101_0000_1100_0101".U)
            c.clock.step(1)
            c.io.symbol_locked.expect(1.U)
            c.io.rx_data_aligned.expect("b100_0001_1_0010_1000_0011".U)

            c.io.rx_user_data_in.poke("b1001_0101_0000_1100_0100".U)
            c.clock.step(1)
            c.io.symbol_locked.expect(1.U)
            c.io.rx_data_aligned.expect("b100_0101_0001_0111_0000_1".U)

            c.clock.step(5)
        }
    }
}
