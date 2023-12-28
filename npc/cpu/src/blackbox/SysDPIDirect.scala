package cpu.blackbox

import chisel3._
import chisel3.util._

import cpu.common._

class SysDPIDirect extends BlackBox with ConfigInst {
    val io = IO(new Bundle {
        val iClock      = Input(Clock())
        val iReset      = Input(Reset())
        val iEbreakFlag = Input(UInt(BYTE_WIDTH.W))
    })
}
