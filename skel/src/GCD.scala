// SPDX-License-Identifier: Apache-2.0

package skel

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.testers.BasicTester

case class GCDParameter(width: Int)

class GCDIO(p: GCDParameter) extends Bundle {
  val a = Input(UInt(p.width.W))
  val b = Input(UInt(p.width.W))
  val e = Input(Bool())
  val z = Output(UInt(p.width.W))
  val v = Output(Bool())
}

class GCD(p: GCDParameter) extends Module {
  val io = IO(new GCDIO(p))
  val x = Reg(UInt(p.width.W))
  val y = Reg(UInt(p.width.W))
  when(x > y) { x := x -% y }.otherwise { y := y -% x }
  when(io.e) { x := io.a; y := io.b }
  io.z := x
  io.v := y === 0.U
}

object EmitVerilog extends App {
  (new circt.stage.ChiselStage).execute(
    Array(
      "--target", "systemverilog",
      "--target-dir", "gcd"
    ),
    Seq(
      chisel3.stage.ChiselGeneratorAnnotation(() => new GCD(GCDParameter(32)))
    )
  )
}