package cpu.blackbox

import chisel3._
import chisel3.util._

import cpu.common._

class MemDPIDirect extends BlackBox with ConfigInst {
    val io = IO(new Bundle {
        val iClock         = Input (Clock())
        val iReset         = Input (Reset())
        val iMemRdEn       = Input (Bool())
        val iMemRdAddrInst = Input (UInt(DATA_WIDTH.W))
        val iMemRdAddrLoad = Input (UInt(DATA_WIDTH.W))
        val iMemWrEn       = Input (Bool())
        val iMemWrAddr     = Input (UInt(DATA_WIDTH.W))
        val iMemWrData     = Input (UInt(DATA_WIDTH.W))
        val iMemWrLen      = Input (UInt(BYTE_WIDTH.W))

        val oMemRdDataInst = Output(UInt(INST_WIDTH.W))
        val oMemRdDataLoad = Output(UInt(DATA_WIDTH.W))
    })
}
