package clxdl

import chisel3._
import chisel3.util._
import tilelink._
import utils._

class SinkA(implicit p: CLXLiteParameters) extends Module {
  val io = IO(new Bundle {
    val a = Flipped(Decoupled(new TLChannelA(p.tl.a)))
    val q = Decoupled(new DataLayer())
  })

  // We need a Q because we stall the channel while serializing it's header
  val inject = Module(new PartialInjector)
  inject.io.i <> Queue(io.a, 1, flow = true)
  inject.io.lastIn := inject.io.i.last
  val a = inject.io.o
  val last = inject.io.lastOut
  val hasData = a.hasData
  val partial = a.bits.opcode === Message.PutPartialData

  // FSM to generate the packet components
  val state = RegInit(0.U(2.W))
  val  stateHeader   = 0.U(2.W)
  val  stateAddress0 = 1.U(2.W)
  val  stateAddress1 = 2.U(2.W)
  val  stateData     = 3.U(2.W)

  when (io.q.fire) {
    switch (state) {
      is ( stateHeader)   { state :=  stateAddress0 }
      is ( stateAddress0) { state :=  stateAddress1 }
      is ( stateAddress1) { state := Mux(hasData,  stateData,  stateHeader) }
      is ( stateData)     { state := Mux(!last,    stateData,  stateHeader) }
    }
  }

  // construct the header beat
  val header = Cat(
    fmt(io.a.bits.source, 16),
    fmt(0.U, 3),
    fmt(io.a.bits.size, 4),
    fmt(io.a.bits.param, 3),
    fmt(io.a.bits.opcode, 3),
    fmt(0.U, 3)
  )

  // construct the address beats
  val address0 = a.bits.address
  val address1 = a.bits.address >> 32

  // frame the output packet
  val isLastState = state === Mux(hasData,  stateData,  stateAddress1)
  a.ready := io.q.ready && isLastState
  io.q.valid := a.valid
  io.q.bits.last  := last && isLastState
  io.q.bits.data  := VecInit(header, address0, address1, a.bits.data)(state)
  io.q.bits.beats := 3.U + Mux(hasData, a.size2beats, 0.U) + Mux(partial, a.mask2beats, 0.U)
}
