package cpu.blackbox

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.port._

class AXI4LiteM extends BlackBox with ConfigInst {
    val io = IO(new Bundle {
        val iClock   = Input (Clock())
        val iReset   = Input (Reset())
        val iMode    = Input (UInt(MODE_WIDTH.W))
        val iRdValid = Input (Bool())
        val iRdAddr  = Input (UInt(ADDR_WIDTH.W))
        val iWrValid = Input (Bool())
        val iWrAddr  = Input (UInt(ADDR_WIDTH.W))
        val iWrData  = Input (UInt(DATA_WIDTH.W))
        val iWrMask  = Input (UInt(MASK_WIDTH.W))

        val oRdData  = Output(UInt(DATA_WIDTH.W))
        val oRdResp  = Output(UInt(RESP_WIDTH.W))
        val oWrResp  = Output(UInt(RESP_WIDTH.W))

        val pAXI4    = new AXI4LiteIO
    })
}
