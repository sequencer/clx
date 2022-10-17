package clx

import chisel3._

class DataLayer(implicit p: CLXLiteParameters) extends Bundle {
  val data = Output(UInt(p.dataBits.W))
  val last = Output(Bool())
  val beats = Output(UInt((p.xferBits + 1).W))
}
