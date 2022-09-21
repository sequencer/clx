package asyncfifo

import chisel3._

object CtrlCharacterTable {
    val SKP = "h1c".U
    val STP = "h7c".U
    val END = "h9c".U
    val COM = "hbc".U
    val PAD = "hdc".U

}
