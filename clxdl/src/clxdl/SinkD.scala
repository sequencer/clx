package clxdl

import chisel3._
import chisel3.util._
import tilelink._
import utils._

class SinkD(implicit p: CLXLiteParameters) extends Module {
  val io = IO(new Bundle {
    val d = Flipped(Decoupled(new TLChannelD(p.tl.d)))
    val q = Decoupled(new DataLayer())
  })

  // The FSM states
  val state = RegInit(0.U(1.W))
  val stateHeader = 0.U(1.W)
  val stateData   = 1.U(1.W)

  // We need a Q because we stall the channel while serializing it's header
  val d = Queue(io.d, 1, flow = true)
  val last = d.last
  val hasData = d.hasData

  when (io.q.fire) {
    switch (state) {
      is (stateHeader)   { state := Mux(hasData, stateData, stateHeader) }
      is (stateData)     { state := Mux(last, stateHeader, stateData) }
    }
  }

  // construct the header beat
  val header = Cat(
    fmt(io.d.bits.source, 16),
    fmt(0.U, 3),
    fmt(io.d.bits.size, 4),
    fmt(io.d.bits.param, 3),
    fmt(io.d.bits.opcode, 3),
    fmt(3.U, 3)
  )

  val isLastState = state === Mux(hasData, stateData, stateHeader)
  d.ready := io.q.ready && isLastState
  io.q.valid := d.valid
  io.q.bits.last  := last && isLastState
  io.q.bits.data  := VecInit(header, d.bits.data)(state)
  io.q.bits.beats := Mux(hasData, d.size2beats, 0.U) + 1.U
}

