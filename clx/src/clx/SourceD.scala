package clx

import chisel3._
import chisel3.util._
import tilelink._
import utils._

class SourceD(implicit p: CLXLiteParameters) extends Module {
  val io = IO(new Bundle {
    val d = Decoupled(new TLChannelD(p.tl.d))
    val q = Flipped(Decoupled(UInt(p.dataBits.W)))
  })
  
  // FSM states
  val state = RegInit(0.U(1.W))
  val stateHeader = 0.U(1.W)
  val stateData   = 1.U(1.W)

  private def hold(key: UInt)(data: UInt): UInt = {
    val enable = state === key
    Mux(enable, data, RegEnable(data, enable))
  }

  // extract header fields from the message
  val opcode = hold(stateHeader)(io.q.bits(5, 3))
  val param = hold(stateHeader)(io.q.bits(8, 6))
  val size = hold(stateHeader)(io.q.bits(12, 9))
  val source = hold(stateHeader)(io.q.bits(31, 16))

  val (_, last) = io.q.firstLast(Some(3.U))
  val stateMaybeData = Mux(last, stateHeader, stateData)

  when(io.q.fire) {
    state := stateMaybeData
  }

  // Look for an available sink
  val xmit = last || state === stateData

  io.d.bits.opcode := opcode
  io.d.bits.param := param(1, 0)
  io.d.bits.size := size
  io.d.bits.source := source
  io.d.bits.sink := 0.U
  io.d.bits.denied := param >> 2
  io.d.bits.data := io.q.bits
  io.d.bits.corrupt := io.d.bits.denied && io.d.hasData

  io.d.valid := io.q.valid && xmit
  io.q.ready := io.d.ready || !xmit
}
