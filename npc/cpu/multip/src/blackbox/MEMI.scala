package cpu.blackbox

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.core._

class MEMPortDualI extends BlackBox with ConfigInst {
    val io = IO(new Bundle {
        val iClock         = Input(Clock())
        val iReset         = Input(Bool())
        val bMEMPortDualIO = new MEMPortDualIO
    })
}
