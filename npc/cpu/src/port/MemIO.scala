package cpu.port

import chisel3._
import chisel3.util._

import cpu.common._

class MemIO extends Bundle with ConfigIO {
    val iRdEn   = Input (Bool())
    val iWrEn   = Input (Bool())
    val iAddr   = Input (UInt(ADDR_WIDTH.W))
    val iWrData = Input (UInt(DATA_WIDTH.W))
    val iWrByt  = Input (UInt(SIGS_WIDTH.W))

    val oRdData = Output(UInt(DATA_WIDTH.W))
}
