package asyncfifo

import chisel3._
import chisel3.util._
import freechips.asyncqueue.{AsyncQueue, AsyncQueueParams}

// wr domain: rxPcsClk
// rd domain: sysClk
class ElasticBufferRx extends RawModule {
    val io = IO(new Bundle {
        // wr
        val clkWr    = Input(Clock())
        val resetWr  = Input(Bool())
        val wr       = Flipped(ValidIO(UInt(18.W)))

        // rd
        val clkRd    = Input(Clock())
        val resetRd  = Input(Bool())
        val rd       = ValidIO(UInt(18.W))
    })

    val asyncFIFOParams = AsyncQueueParams(256, 3, safe = true, narrow = false)
    val asyncFIFO = withClockAndReset(io.clkWr, io.resetWr) { Module(new AsyncQueue(UInt(18.W), asyncFIFOParams)) }

    // filter out {COM + SKP}
    val skipFlag = WireDefault(false.B)
    private val skpCom = Cat(CtrlCharacterTable.SKP, CtrlCharacterTable.COM)
    when ((io.wr.bits(17, 16) === "b11".U) &&  (io.wr.bits(15, 0) ===  skpCom)) {
        skipFlag := true.B
    }

    // wr <> enq
    asyncFIFO.io.enq_clock := io.clkWr
    asyncFIFO.io.enq_reset := io.resetWr
    asyncFIFO.io.enq.valid := io.wr.valid && (!skipFlag)
    asyncFIFO.io.enq.bits  := io.wr.bits
    withClockAndReset(io.clkWr, io.resetWr) { assert(asyncFIFO.io.enq.ready === 1.U) }

    // rd <> deq
    asyncFIFO.io.deq_clock := io.clkRd
    asyncFIFO.io.deq_reset := io.resetRd
    asyncFIFO.io.deq.ready := 1.U // rx data path should be non-blocking
    io.rd.valid := asyncFIFO.io.deq.valid
    io.rd.bits := asyncFIFO.io.deq.bits
}

object ElasticBufferRxV extends App {
    (new chisel3.stage.ChiselStage).emitVerilog(new ElasticBufferRx)
}