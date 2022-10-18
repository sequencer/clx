package clxdl

import chisel3._
import chisel3.util._
import utils._

class CreditBump(implicit p: CLXLiteParameters) extends Bundle {
  val a = Output(UInt(p.creditBits.W))
  val d = Output(UInt(p.creditBits.W))
  def X: Seq[UInt] = Seq(a, d)

  // saturating addition
  def +(that: CreditBump): CreditBump = {
    val out = Wire(new CreditBump())
    (out.X zip (X zip that.X)) foreach { case (o, (x, y)) =>
      val z = x +& y
      o := Mux((z >> p.creditBits).asUInt.orR, ~0.U(p.creditBits.W), z)
    }
    out
  }

  // Send the MSB of the credits
  def toHeader: (UInt, CreditBump) = {
    def msb(x: UInt): (UInt, UInt) = {
      val mask = rightOR(x) >> 1
      val msbOH: UInt = ~(~x | mask)
      val msb = OHToUInt(msbOH << 1, p.creditBits + 1) // 0 = 0, 1 = 1, 2 = 4, 3 = 8, ...
      val pad = (msb | 0.U(5.W))(4,0)
      (pad, x & mask)
    }
    val (a_msb, a_rest) = msb(a)
    val (d_msb, d_rest) = msb(d)
    val header = Cat(
      0.U(5.W), d_msb, 0.U(5.W), 0.U(5.W), a_msb,
      0.U(4.W), // padding
      5.U(3.W))

    val out = Wire(new CreditBump())
    out.a := a_rest
    out.d := d_rest
    (header, out)
  }
}

object CreditBump {
  def apply(x: Int)(implicit p: CLXLiteParameters): CreditBump = {
    val v = x.U(p.creditBits.W)
    val out = Wire(new CreditBump())
    out.X.foreach { _ := v }
    out
  }

  def apply(header: UInt)(implicit p: CLXLiteParameters): CreditBump = {
    def convert(x: UInt) =
      Mux(x > p.creditBits.U,
        ~0.U(p.creditBits.W),
        UIntToOH(x, p.creditBits + 1) >> 1)
    val out = Wire(new CreditBump())
    out.a := convert(header(11,  7))
    out.d := convert(header(26, 22))
    out
  }
}
