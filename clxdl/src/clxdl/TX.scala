package clxdl

import chisel3._
import chisel3.util._
import utils._

class TX(implicit p: CLXLiteParameters) extends Module {
  val io = IO(new Bundle {
    val c2b = ValidIO(UInt(p.dataBits.W))
    val a = Flipped(DecoupledIO(new DataLayer()))
    val d = Flipped(DecoupledIO(new DataLayer()))
    val rxc = Flipped(new CreditBump())
    val txc = Flipped(new CreditBump())
  })

  // currently available credits
  val rx = RegInit(CreditBump(0))
  val tx = RegInit(CreditBump(0))

  // credits from RX
  val rxInc = io.rxc
  val txInc = io.txc

  val qa = ShiftQueue(io.a, 2)
  val qd = ShiftQueue(io.d, 2)
  private def qX: Seq[DecoupledIO[DataLayer]] = Seq(qa, qd)

  // Consume TX credits and propagate pre-paid requests
  val ioX: Seq[DecoupledIO[DataLayer]] = (qX zip (tx.X zip txInc.X)) map { case (q, (credit, gain)) =>
    val first = RegEnable(q.bits.last, true.B, q.fire)
    val delta = credit -& q.bits.beats
    val allow = !first || (delta.asSInt >= 0.S)
    credit := Mux(q.fire && first, delta, credit) + gain

    val cq = Module(new ShiftQueue(chiselTypeOf(q.bits), 2)) // maybe flow?
    cq.io.enq.bits := q.bits
    cq.io.enq.valid := q.valid && allow
    q.ready := cq.io.enq.ready && allow
    cq.io.deq
  }

  // prepare RX credit update headers
  val rxQ = Module(new ShiftQueue(new DataLayer(), 2)) // maybe flow?
  val (rxHeader, rxLeft) = rx.toHeader
  rxQ.io.enq.valid := true.B
  rxQ.io.enq.bits.data  := rxHeader
  rxQ.io.enq.bits.last  := true.B
  rxQ.io.enq.bits.beats := 1.U
  rx := Mux(rxQ.io.enq.fire, rxLeft, rx) + rxInc

  // include the F credit channel in arbitration
  val f = WireInit(rxQ.io.deq)
  val ioF = ioX :+ f
  val requests = Cat(ioF.map(_.valid).reverse)
  val lasts = Cat(ioF.map(_.bits.last).reverse)

  // How often should we force transmission of a credit update? sqrt
  val xmitBits = log2Ceil(p.qDepth) / 2
  val xmit = RegInit(0.U(xmitBits.W))
  val forceXmit = xmit === 0.U
  when (!forceXmit) { xmit := xmit - 1.U }
  when (f.fire) { xmit := ~0.U(xmitBits.W) }

  // Flow control for returned credits
  val allowReturn = !ioX.map(_.valid).reduce(_ || _) || forceXmit
  f.bits  := rxQ.io.deq.bits
  f.valid := rxQ.io.deq.valid && allowReturn
  rxQ.io.deq.ready := f.ready && allowReturn

  // Select a channel to transmit from those with data and space
  val first = RegInit(true.B)
  val state = RegInit(0.U(3.W))
  val readys = Arbiter.roundRobin(3, requests, first)
  val winner = readys & requests
  val grant = Mux(first, winner, state)
  val allowed = Mux(first, readys, state)
  (ioF zip allowed.asBools) foreach { case (beat, sel) => beat.ready := sel }

  val send = Mux(first, rxQ.io.deq.valid, (state & requests) =/= 0.U)
  assert (send === ((grant & requests) =/= 0.U))

  when (send) { first := (grant & lasts).orR }
  when (first) { state := winner }

  // form the output beat
  io.c2b.valid := RegNext(RegNext(send, false.B), false.B)
  io.c2b.bits := RegNext(Mux1H(RegNext(grant), RegNext(VecInit(ioF.map(_.bits.data)))))
}
