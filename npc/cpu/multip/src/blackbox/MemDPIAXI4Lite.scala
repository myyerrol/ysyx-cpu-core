package cpu.blackbox

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.core._

class MemDPIAXI4LiteLFU extends BlackBox with ConfigInst {
    val io = IO(new Bundle {
        val iClock           = Input(Clock())
        val iReset           = Input(Reset())
        val bIFUAXISlaveARIO = Flipped(new IFUAXI4LiteARIO)
        val bIFUAXISlaveRIO  = Flipped(new IFUAXI4LiteRIO)
    })
}
