package clxdl

import chisel3._
import chisel3.util._
import tilelink._

/**
  * @example {{{
  *                   next shiftBuffer                    dataOut
  * state 0:                             mddd[dddddmddddddddmddddddddmdddddddd]
  *                                               3        2        1        0
  * state 1:                         mddddddd[dmddddddddmddddddddmddddddddmddd]
  *                                           7        6        5        4
  * state 2:                     mddddddddmdd[ddddddmddddddddmddddddddmddddddd]
  *                                               10        9        8
  * state 3:                 mddddddddmdddddd[ddmddddddddmddddddddmddddddddmdd]
  *                                           14       13       12       11
  * state 4:             mddddddddmddddddddmd[dddddddmddddddddmddddddddmdddddd]
  *                                                17       16       15
  * state 5:         mddddddddmddddddddmddddd[dddmddddddddmddddddddmddddddddmd]
  *                                            21       20        19       18
  * state 6:     mddddddddmddddddddmddddddddm[ddddddddmddddddddmddddddddmddddd]
  *                                                 21       20       19
  * state 7: mddddddddmddddddddmddddddddmdddd[ddddmddddddddmddddddddmddddddddm]
  *                                             25       24       23       22
  * state 8:                                 [mddddddddmddddddddmddddddddmdddd]
  *                                                  28       27       26
  * }}}
  */
class PartialInjector(implicit p: CLXLiteParameters) extends Module {
  val io = IO(new Bundle {
    val i = Flipped(Decoupled(new TLChannelA(p.tl.a)))
    val o =         Decoupled(new TLChannelA(p.tl.a))
    val lastIn  = Input (Bool())
    val lastOut = Output(Bool())
    val partial = Output(Bool())
  })

  io.o <> io.i

  val opcode = io.i.bits.opcode
  val mask = io.i.bits.mask

  val dataIn = io.i.bits.data
  val dataOut = io.o.bits.data

  val state = RegInit(0.U(4.W)) // [0, 8]
  val full  = state(3) // state === 8.U
  io.partial := opcode === Message.PutPartialData

  val last = RegInit(false.B)
  io.lastOut := Mux(io.partial, last, io.lastIn)
  
  val shiftBuffer = RegInit(0.U(32.W))
  
  when (io.partial) {
    val dataBytes = Seq.tabulate(4) { i => dataIn(8 * (i + 1) - 1, 8 * i) }
    val maskBits  = mask.asBools
    val mixed = Cat(Seq(maskBits, dataBytes).transpose.flatten.reverse) // (32 + 4) = 36b
    val wide  = shiftBuffer | (mixed << (state << 2)) // (36 + 2 ^ (4 + 2) - 1) = 99b
    dataOut := wide(31, 0)

    // Inject a beat
    when ((io.lastIn || full) && !last) {
      io.i.ready := false.B
    }

    // Update the FSM
    when (io.o.fire) {
      shiftBuffer := wide >> 32
      state := state + 1.U
      when (full || last) {
        state := 0.U
        shiftBuffer := 0.U
      }
      last := io.lastIn && !last
    }
  }
}


class PartialExtractor(implicit p: CLXLiteParameters) extends Module {
  val io = IO(new Bundle {
    val last = Input(Bool())
    val i = Flipped(Decoupled(new TLChannelA(p.tl.a)))
    val o = Decoupled(new TLChannelA(p.tl.a))
  })

  io.o <> io.i

  val opcode = io.i.bits.opcode

  val mask = io.o.bits.mask

  val dataIn = io.i.bits.data
  val dataOut = io.o.bits.data

  val state = RegInit(0.U(4.W)) // number of nibbles; [0,8]
  val shiftBuffer = RegInit(0.U(32.W))
  val enable = opcode === Message.PutPartialData
  val empty  = state === 0.U

  when (enable) {
    val wide = shiftBuffer | (dataIn << (state << 2))
    dataOut := VecInit.tabulate(4) { i => wide(9*(i+1)-1, 9*i+1) } .asUInt
    mask := VecInit.tabulate(4) { i => wide(9*i) } .asUInt

    // Swallow beat if we have no nibbles
    when (empty) {
      io.i.ready := true.B
      io.o.valid := false.B
    }

    // Update the FSM
    when (io.i.fire) {
      shiftBuffer := Mux(empty, dataIn, wide >> 36)
      state := state - 1.U
      when (empty)   { state := 8.U }
      when (io.last) { state := 0.U }
    }
  }
}


