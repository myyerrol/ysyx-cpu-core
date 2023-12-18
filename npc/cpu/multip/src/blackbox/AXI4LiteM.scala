package cpu.blackbox

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.port._

class AXI4LiteM extends BlackBox with ConfigInst {
    val io = IO(new Bundle {
        val iClock = Input (Clock())
        val iReset = Input (Reset())
        val iMode  = Input (UInt(MODE_WIDTH.W))
        val iAddr  = Input (UInt(ADDR_WIDTH.W))
        val iData  = Input (UInt(DATA_WIDTH.W))
        val iMask  = Input (UInt(MASK_WIDTH.W))
        val oData  = Output(UInt(DATA_WIDTH.W))
        val oResp  = Output(UInt(RESP_WIDTH.W))

        val pAXI4  = new AXI4LiteIO
    })
}
