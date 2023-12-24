package cpu.blackbox

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.port._

class AXI4LiteS extends BlackBox with ConfigInst {
    val io = IO(new Bundle {
        val iClock   = Input (Clock())
        val iReset   = Input (Reset())
        val iMode    = Input (UInt(MODE_WIDTH.W))
        val iRdData  = Input (UInt(DATA_WIDTH.W))
        val iResp    = Input (UInt(RESP_WIDTH.W))

        val oRdAddr  = Output(UInt(ADDR_WIDTH.W))
        val oWrAddr  = Output(UInt(ADDR_WIDTH.W))
        val oWrData  = Output(UInt(DATA_WIDTH.W))
        val oWrMask  = Output(UInt(MASK_WIDTH.W))

        val pAXI4    = Flipped(new AXI4LiteIO)
    })
}
