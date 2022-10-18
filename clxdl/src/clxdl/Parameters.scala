package clxdl

import chisel3.util._
import tilelink._

case class CLXLiteParameters() {
  val tl = TLBundleParameter(
    a = TLChannelAParameter(
      addressWidth = 64, // not adjustable
      sourceWidth = 6,   // adjustable, max = 16
      dataWidth = 32,    // not adjustable
      sizeWidth = 4,     // adjustable, max = 4
      maskWidth = 4      // not adjustable
    ),
    b = None,
    c = None,
    d = TLChannelDParameter(
      sourceWidth = 6, // adjustable, max = 16
      sinkWidth = 6,   // adjustable, const 0
      dataWidth = 32,  // not adjustable
      sizeWidth = 4    // adjustable, max = 4
    ),
    e = None
  )

  val dataBytes = 4 // not adjustable, width of DataLayer, 32 / 8
  val maxXfer = 4096 // adjustable
  val creditBits = 20 // use saturating addition => we can exploit at most 1MB of buffers
  val qSizeBytes = 8192 // adjustable, must be a multiply of 4

  // derived parameters
  val dataBits = dataBytes * 8
  val xferBits = log2Ceil(maxXfer)
  val beatBytes = dataBytes
  val qDepth = qSizeBytes / dataBytes
}
