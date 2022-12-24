//package codec
//
//import chisel3._
//import chiseltest._
//import scala.util.Random
//import Array._
//import org.scalatest.flatspec.AnyFlatSpec
//
//class EncoderTestFull extends AnyFlatSpec with ChiselScalatestTester{
//    behavior of "Encoder8b10b"
//    it should "encode all table entries" in {
//        test(new Encoder8b10b).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
//            var rdOfTestbench = false
//            var encoded = 0.U(10.W)
//            val dataArray = List.range(0, EncodeTestTable.D_x_y.length, 1)
//            val ctrlArray = List.range(0, EncodeTestTable.K_28_y.length, 1)
//
//            c.clock.step()
//            for (i <- Random.shuffle(dataArray)) {
//                c.io.isControlCharacter.poke(0.U) // data
//                c.io.decoded8bInput.poke(EncodeTestTable.D_x_y(i)(0))
//                if (!c.io.runningDisparity.peekBoolean()) { //
//                    encoded = EncodeTestTable.D_x_y(i)(1)
//                } else {
//                    encoded = EncodeTestTable.D_x_y(i)(2)
//                }
//
//                c.clock.step()
//                val shouldFlip = (EncodeTestTable.D_x_y(i)(3).litValue == 1)
//                if (shouldFlip) {
//                    rdOfTestbench = !rdOfTestbench // flip
//                }
//                c.io.runningDisparity.expect(rdOfTestbench.asBool)
//                c.io.encoded10bOutput.expect(encoded)
//            }
//
//            for (i <- Random.shuffle(ctrlArray)) {
//                c.io.isControlCharacter.poke(1.U) // control
//                c.io.decoded8bInput.poke(EncodeTestTable.K_28_y(i)(0))
//                if (!c.io.runningDisparity.peekBoolean()) { //
//                    encoded = EncodeTestTable.K_28_y(i)(1)
//                } else {
//                    encoded = EncodeTestTable.K_28_y(i)(2)
//                }
//
//                c.clock.step()
//                val shouldFlip = (EncodeTestTable.K_28_y(i)(3).litValue == 1)
//                if (shouldFlip) {
//                    rdOfTestbench = !rdOfTestbench // flip
//                }
//                c.io.runningDisparity.expect(rdOfTestbench.asBool)
//                c.io.encoded10bOutput.expect(encoded)
//            }
//        }
//    }
//}
