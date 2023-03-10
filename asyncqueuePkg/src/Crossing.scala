// See LICENSE.SiFive for license details.

package freechips.asyncqueue

import Chisel._
import chisel3.util.Decoupled

class CrossingIO[T <: Data](gen: T) extends Bundle {
    // Enqueue clock domain
    val enq_clock = Clock(INPUT)
    val enq_reset = Bool(INPUT) // synchronously deasserted wrt. enq_clock
    val enq = Decoupled(gen).flip
    // Dequeue clock domain
    val deq_clock = Clock(INPUT)
    val deq_reset = Bool(INPUT) // synchronously deasserted wrt. deq_clock
    val deq = Decoupled(gen)
}

abstract class Crossing[T <: Data] extends Module {
    val io: CrossingIO[T]
}
