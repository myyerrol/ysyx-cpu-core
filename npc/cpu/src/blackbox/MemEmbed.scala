package cpu.blackbox

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.port._

class MemEmbed extends BlackBox with ConfigInst {
    val io = IO(new Bundle {
        val iClock = Input(Clock())
        val iReset = Input(Reset())
        val pMem   = new MemIO
    })
}
