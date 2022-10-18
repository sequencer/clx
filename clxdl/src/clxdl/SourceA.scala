package clxdl

import chisel3._
import chisel3.util._
import tilelink._
import utils._

class SourceA(implicit p: CLXLiteParameters) extends Module {
  val io = IO(new Bundle {
    val a = Decoupled(new TLChannelA(p.tl.a))
    val q = Flipped(Decoupled(UInt(p.dataBits.W)))
  })

  // A simple FSM to generate the packet components
  val state = RegInit(0.U(2.W))
  val stateHeader   = 0.U(2.W)
  val stateAddress0 = 1.U(2.W)
  val stateAddress1 = 2.U(2.W)
  val stateData     = 3.U(2.W)

  private def hold(key: UInt)(data: UInt) = {
    val enable = state === key
    Mux(enable, data, RegEnable(data, enable))
  }

  // extract header fields
  val opcode = hold(stateHeader)(io.q.bits(5, 3))
  val param = hold(stateHeader)(io.q.bits(8, 6))
  val size = hold(stateHeader)(io.q.bits(12, 9))
  val source = hold(stateHeader)(io.q.bits(31, 16))

  // latch address
  val address0 = hold(stateAddress0)(io.q.bits)
  val address1 = hold(stateAddress1)(io.q.bits)
  val address = Cat(address1, address0)

  val (_, last) = io.q.firstLast(Some(0.U))
  val hasData = !opcode(2)
  val first = RegEnable(state =/= stateData, io.q.fire)

  when (io.q.fire) {
    switch (state) {
      is (stateHeader)   { state := stateAddress0 }
      is (stateAddress0) { state := stateAddress1 }
      is (stateAddress1) { state := Mux(hasData, stateData, stateHeader) }
      is (stateData)     { state := Mux(!last,   stateData, stateHeader) }
    }
  }

  // Feed our preliminary A channel via the Partial Extractor FSM
  val extract = Module(new PartialExtractor())
  io.a <> extract.io.o
  val a = extract.io.i
  extract.io.last := last

  a.bits.opcode  := opcode
  a.bits.param   := param
  a.bits.size    := size
  a.bits.source  := source
  a.bits.address := address
  a.bits.mask    := MaskGen(address0, size, p.dataBytes)
  a.bits.data    := io.q.bits
  a.bits.corrupt := false.B

  val xmit = last || state === stateData
  a.valid := io.q.valid &&  xmit
  io.q.ready := a.ready || !xmit
}

