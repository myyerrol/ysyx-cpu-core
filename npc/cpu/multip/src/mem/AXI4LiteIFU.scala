package cpu.mem

import chisel3._
import chisel3.util._

import cpu.blackbox._
import cpu.common._
import cpu.port._

class AXI4LiteIFU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iRdValid = Input (Bool())
        val iRdAddr  = Input (UInt(ADDR_WIDTH.W))

        val oRdData  = Output(UInt(DATA_WIDTH.W))

        val pAXI4    = new AXI4LiteIO
    })

    val mAXI4LiteM = Module(new AXI4LiteM)
    mAXI4LiteM.io.iClock   := clock;
    mAXI4LiteM.io.iReset   := reset;
    mAXI4LiteM.io.iMode    := MODE_RD
    mAXI4LiteM.io.iRdValid := io.iRdValid
    mAXI4LiteM.io.iRdAddr  := io.iRdAddr
    mAXI4LiteM.io.iWrValid := DontCare
    mAXI4LiteM.io.iWrAddr  := DontCare
    mAXI4LiteM.io.iWrData  := DontCare
    mAXI4LiteM.io.iWrMask  := DontCare

    io.oRdData := mAXI4LiteM.io.oRdData

    io.pAXI4 <> mAXI4LiteM.io.pAXI4
}
