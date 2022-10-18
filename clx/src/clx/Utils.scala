package clx

import chisel3._
import chisel3.util._
import tilelink._

package object utils {
  def fmt(x: UInt, w: Int) = x.pad(w)(w - 1, 0)

  private def makeBeats(size: UInt, shift: Int)(implicit p: CLXLiteParameters): UInt =
    Cat(UIntToOH(size | 0.U(4.W), p.xferBits + 1) >> (shift + 1), size <= shift.U)

  private def size2BeatsCommon(size: UInt)(implicit p: CLXLiteParameters): UInt = makeBeats(size, log2Ceil(p.dataBytes))

  private def mask2BeatsCommon(size: UInt)(implicit p: CLXLiteParameters): UInt = makeBeats(size, log2Ceil(p.dataBytes * 8))

  implicit class TLChannelAUtils(a: DecoupledIO[TLChannelA])(implicit p: CLXLiteParameters) {
    def hasData: Bool = !a.bits.opcode(2)
        //    opcode === TLMessages.PutFullData    ||
        //    opcode === TLMessages.PutPartialData ||
        //    opcode === TLMessages.ArithmeticData ||
        //    opcode === TLMessages.LogicalData

    def last: Bool = {
      def numBeats1(x: TLChannelA): UInt = {
        /** UIntToOH minus 1 */
        def UIntToOH1(x: UInt, width: Int): UInt = ~((-1).S(width.W).asUInt << x) (width - 1, 0)

        val decode = UIntToOH1(a.bits.size, p.xferBits) >> log2Ceil(p.beatBytes)
        Mux(hasData, decode, 0.U)
      }

      /** beats minus 1 */
      val beats1 = numBeats1(a.bits)
      val counter = RegInit(0.U(log2Up(p.maxXfer / p.beatBytes).W))
      /** counter minus 1 */
      val counter1 = counter - 1.U
      val first = counter === 0.U
      val last = counter === 1.U || beats1 === 0.U
      when(a.fire) {
        counter := Mux(first, beats1, counter1)
      }
      last
    }

    def size2beats: UInt = size2BeatsCommon(a.bits.size)

    def mask2beats: UInt = mask2BeatsCommon(a.bits.size)
  }

  implicit class TLChannelDUtils(d: DecoupledIO[TLChannelD])(implicit p: CLXLiteParameters) {
    def hasData: Bool = d.bits.opcode(0)
        // opcode === TLMessages.AccessAckData ||
        // opcode === TLMessages.GrantData

    def size2beats: UInt = size2BeatsCommon(d.bits.size)

    def last: Bool = {
      val count = RegInit(0.U((p.xferBits + 1).W))
      val beats = Mux(d.bits.opcode(0), size2beats(d.bits.size), 0.U)
      val first = count === 0.U
      val last = count === 1.U || (first && beats === 0.U)
      when(d.fire) {
        count := Mux(first, beats, count - 1.U)
      }
      last
    }
  }

  implicit class RawDataUtils(r: DecoupledIO[UInt])(implicit p: CLXLiteParameters) {
    private def beats1(x: UInt, forceFormat: Option[UInt] = None): UInt = {
      val format = x(2, 0)
      val opcode = x(5, 3)
      val size = x(12, 9)
      val beats = size2BeatsCommon(size)
      val masks = mask2BeatsCommon(size)
      val partial = opcode === Message.PutPartialData
      val a = Mux(opcode(2), 0.U, beats) + 2.U + Mux(partial, masks, 0.U)
      val d = Mux(opcode(0), beats, 0.U)
      val f = 0.U
      VecInit(a, 0.U, 0.U, d, 0.U, f)(forceFormat.getOrElse(format))
    }

    def firstLast(forceFormat: Option[UInt] = None): (Bool, Bool) = {
      val count = RegInit(0.U((p.xferBits+1).W))
      val beats = beats1(r.bits, forceFormat)
      val first = count === 0.U
      val last = count === 1.U || (first && beats === 0.U)
      when(r.fire) {
        count := Mux(first, beats, count - 1.U)
      }
      (first, last)
    }
  }


  /** Implements the same interface as chisel3.util.Queue, but uses a shift
    * register internally.  It is less energy efficient whenever the queue
    * has more than one entry populated, but is faster on the dequeue side.
    * It is efficient for usually-empty flow-through queues. */
  class ShiftQueue[T <: Data](gen: T,
                              val entries: Int,
                              pipe: Boolean = false,
                              flow: Boolean = false)
    extends Module {
    val io = IO(new QueueIO(gen, entries) {
      val mask = Output(UInt(entries.W))
    })

    private val valid = RegInit(VecInit(Seq.fill(entries) {
      false.B
    }))
    private val elts = Reg(Vec(entries, gen))

    for (i <- 0 until entries) {
      def paddedValid(i: Int) = if (i == -1) true.B else if (i == entries) false.B else valid(i)

      val wdata = if (i == entries - 1) io.enq.bits else Mux(valid(i + 1), elts(i + 1), io.enq.bits)
      val wen =
        Mux(io.deq.ready,
          paddedValid(i + 1) || io.enq.fire && ((i == 0 && !flow).B || valid(i)),
          io.enq.fire && paddedValid(i - 1) && !valid(i))
      when(wen) {
        elts(i) := wdata
      }

      valid(i) :=
        Mux(io.deq.ready,
          paddedValid(i + 1) || io.enq.fire() && ((i == 0 && !flow).B || valid(i)),
          io.enq.fire() && paddedValid(i - 1) || valid(i))
    }

    io.enq.ready := !valid(entries - 1)
    io.deq.valid := valid(0)
    io.deq.bits := elts.head

    if (flow) {
      when(io.enq.valid) {
        io.deq.valid := true.B
      }
      when(!valid(0)) {
        io.deq.bits := io.enq.bits
      }
    }

    if (pipe) {
      when(io.deq.ready) {
        io.enq.ready := true.B
      }
    }

    io.mask := valid.asUInt
    io.count := PopCount(io.mask)
  }

  object ShiftQueue {
    def apply[T <: Data](enq: DecoupledIO[T], entries: Int = 2, pipe: Boolean = false, flow: Boolean = false): DecoupledIO[T] = {
      val q = Module(new ShiftQueue(enq.bits.cloneType, entries, pipe, flow))
      q.io.enq <> enq
      q.io.deq
    }
  }

  // Fill 1s from low bits to high bits
  def leftOR(x: UInt): UInt = leftOR(x, x.getWidth, x.getWidth)

  def leftOR(x: UInt, width: Integer, cap: Integer = 999999): UInt = {
    val stop = math.min(width, cap)

    def helper(s: Int, x: UInt): UInt =
      if (s >= stop) x else helper(s + s, x | (x << s) (width - 1, 0))

    helper(1, x)(width - 1, 0)
  }


  // Fill 1s form high bits to low bits
  def rightOR(x: UInt): UInt = rightOR(x, x.getWidth, x.getWidth)

  def rightOR(x: UInt, width: Integer, cap: Integer = 999999): UInt = {
    val stop = math.min(width, cap)

    def helper(s: Int, x: UInt): UInt =
      if (s >= stop) x else helper(s + s, x | (x >> s))

    helper(1, x)(width - 1, 0)
  }

  object Arbiter {
    // (valids, select) => readys
    type Policy = (Integer, UInt, Bool) => UInt

    val roundRobin: Policy = (width, valids, select) => if (width == 1) 1.U(1.W) else {
      val valid = valids(width - 1, 0)
      assert(valid === valids)
      val mask = RegInit(((BigInt(1) << width) - 1).U(width - 1, 0))
      val filter = Cat(valid & ~mask, valid)
      val unready = (rightOR(filter, width * 2, width) >> 1) | (mask << width)
      val readys = ~((unready >> width) & unready(width - 1, 0))
      when(select && valid.orR) {
        mask := leftOR(readys & valid, width)
      }
      readys(width - 1, 0)
    }
  }

  // This gets used everywhere, so make the smallest circuit possible ...
  // Given an address and size, create a mask of beatBytes size
  // eg: (0x3, 0, 4) => 0001, (0x3, 1, 4) => 0011, (0x3, 2, 4) => 1111
  // groupBy applies an interleaved OR reduction; groupBy=2 take 0010 => 01
  object MaskGen {
    def apply(addr_lo: UInt, lgSize: UInt, beatBytes: Int, groupBy: Int = 1): UInt = {
      require(groupBy >= 1 && beatBytes >= groupBy)
      require(isPow2(beatBytes) && isPow2(groupBy))
      val lgBytes = log2Ceil(beatBytes)
      val sizeOH = UIntToOH(lgSize | 0.U(log2Up(beatBytes).W), log2Up(beatBytes)) | (groupBy * 2 - 1).U

      def helper(i: Int): Seq[(Bool, Bool)] = {
        if (i == 0) {
          Seq((lgSize >= lgBytes.U, true.B))
        } else {
          val sub = helper(i - 1)
          val size = sizeOH(lgBytes - i)
          val bit = addr_lo(lgBytes - i)
          val nbit = !bit
          Seq.tabulate(1 << i) { j =>
            val (sub_acc, sub_eq) = sub(j / 2)
            val eq = sub_eq && (if (j % 2 == 1) bit else nbit)
            val acc = sub_acc || (size && eq)
            (acc, eq)
          }
        }
      }

      if (groupBy == beatBytes) 1.U else
        Cat(helper(lgBytes - log2Ceil(groupBy)).map(_._1).reverse)
    }
  }

  class HellaFlowQueue[T <: Data](val entries: Int)(data: => T) extends Module {
    val io = IO(new QueueIO(data, entries))
    require(entries > 1)

    val do_flow = Wire(Bool())
    val do_enq = io.enq.fire && !do_flow
    val do_deq = io.deq.fire && !do_flow

    val maybe_full = RegInit(false.B)
    val enq_ptr = Counter(do_enq, entries)._1
    val (deq_ptr, deq_done) = Counter(do_deq, entries)
    when(do_enq =/= do_deq) {
      maybe_full := do_enq
    }

    val ptr_match = enq_ptr === deq_ptr
    val empty = ptr_match && !maybe_full
    val full = ptr_match && maybe_full
    val atLeastTwo = full || enq_ptr - deq_ptr >= 2.U
    do_flow := empty && io.deq.ready

    val ram = SyncReadMem(entries, data)
    when(do_enq) {
      ram.write(enq_ptr, io.enq.bits)
    }

    // BUG! does not hold the output of the SRAM when !ready
    // ... However, HellaQueue is correct due to the pipe stage
    val ren = io.deq.ready && (atLeastTwo || !io.deq.valid && !empty)
    val raddr = Mux(io.deq.valid, Mux(deq_done, 0.U, deq_ptr + 1.U), deq_ptr)
    val ram_out_valid = RegNext(ren)

    io.deq.valid := Mux(empty, io.enq.valid, ram_out_valid)
    io.enq.ready := !full
    io.deq.bits := Mux(empty, io.enq.bits, ram.read(raddr, ren))
    io.count := DontCare
  }

  class HellaQueue[T <: Data](val entries: Int)(data: => T) extends Module {
    val io = IO(new QueueIO(data, entries))

    val fq = Module(new HellaFlowQueue(entries)(data))
    fq.io.enq <> io.enq
    io.deq <> Queue(fq.io.deq, 1, pipe = true)
    io.count := fq.io.count
  }
}
