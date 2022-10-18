// SPDX-License-Identifier: Apache-2.0

package clx

import chisel3._
import chisel3.util._
import chisel3.stage._

class CLXLite(implicit p: CLXLiteParameters) extends Module {
  val io = IO(new Bundle {
    val tlSlave = Flipped(p.tl.bundle())
    val tlMaster = p.tl.bundle()
    val c2b = ValidIO(UInt(p.dataBits.W))
    val b2c = Flipped(ValidIO(UInt(p.dataBits.W)))
  })

  val sinkA = Module(new SinkA())
  sinkA.io.a <> io.tlSlave.a

  val sourceD = Module(new SourceD())
  io.tlSlave.d <> sourceD.io.d

  val sourceA = Module(new SourceA())
  io.tlMaster.a <> sourceA.io.a

  val sinkD = Module(new SinkD())
  sinkD.io.d <> io.tlMaster.d

  val tx = Module(new TX())
  tx.io.a <> sinkA.io.q
  tx.io.d <> sinkD.io.q
  io.c2b <> tx.io.c2b

  val rx = Module(new RX())
  sourceA.io.q <> rx.io.a
  sourceD.io.q <> rx.io.d
  rx.io.b2c <> io.b2c

  tx.io.txc <> rx.io.txc
  tx.io.rxc <> rx.io.rxc
}

object EmitVerilog extends App {
  val p = CLXLiteParameters()

  (new ChiselStage).execute(
    Array(
      "--target-dir", "tmp"
    ),
    Seq(
      ChiselGeneratorAnnotation(() => new CLXLite()(p))
    )
  )
}