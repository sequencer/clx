package clxdl

import chisel3._
import chisel3.util._
import utils._

class RX(implicit p: CLXLiteParameters) extends Module {
  val io = IO(new Bundle {
    val b2c = Flipped(ValidIO(UInt(p.dataBits.W)))
    val a = DecoupledIO(UInt(p.dataBits.W))
    val d = DecoupledIO(UInt(p.dataBits.W))
    val rxc = new CreditBump()
    val txc = new CreditBump()
  })

  // fit b2c into the firstLast API
  val beat = Wire(Decoupled(UInt(p.dataBits.W)))
  beat.bits  := io.b2c.bits
  beat.valid := io.b2c.valid
  beat.ready := true.B

  // select the correct HellaQueue for the request
  val (first, _) = beat.firstLast()
  val formatBits  = beat.bits(2, 0)
  val formatValid = beat.fire && first
  val format = Mux(formatValid, formatBits, RegEnable(formatBits, formatValid))
  val formats = Seq(format === 0.U, format === 3.U)

  // create the receiver buffers
  val hqa = Module(new HellaQueue(p.qDepth)(chiselTypeOf(beat.bits)))
  val hqd = Module(new HellaQueue(p.qDepth)(chiselTypeOf(beat.bits)))

  // use these to save some typing; function to prevent renaming
  private def hqX = Seq(hqa, hqd)
  private def ioX = Seq(io.a, io.d)

  // enqueue to the HellaQueues
  (formats zip hqX) foreach { case (sel, hq) =>
    hq.io.enq.valid := beat.valid && sel
    hq.io.enq.bits := beat.bits
    assert (!hq.io.enq.valid || hq.io.enq.ready) // overrun impossible
  }

  // send HellaQueue output to their respective FSMs
  (hqX zip ioX) foreach { case (hq, io) =>
    io <> hq.io.deq
  }

  // credits we need to hand-off to the TX FSM
  val tx = RegInit(CreditBump(0))
  val rx = RegInit(CreditBump(p.qDepth))

  // constantly transmit credit updates
  io.txc <> tx
  io.rxc <> rx

  // generate new RX credits as the HellaQueues drain
  (hqX zip rx.X) foreach { case (hq, inc) =>
    inc := hq.io.deq.fire.asUInt
  }

  // generate new TX credits as we receive F-format messages
  tx := Mux(beat.valid && format === 5.U, CreditBump(beat.bits), CreditBump(0))
}
