package linktraining

import chisel3._

class LinkTrainerConnector extends Module {
    val io = IO(new Bundle {
        val upLinked = Output(Bool())
        val dnLinked = Output(Bool())
    })

    val up = Module(new LinkTrainerUp)
    val dn = Module(new LinkTrainerDn)

    up.io.rxDataIn  := dn.io.txDataOut
    dn.io.rxDataIn  := up.io.txDataOut
    io.upLinked     := up.io.linkedUp
    io.dnLinked     := dn.io.linkedUp
}
