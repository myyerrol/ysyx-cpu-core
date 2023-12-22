package cpu.mem

import chisel3._
import chisel3.util._

import cpu.blackbox._
import cpu.common._
import cpu.port._

class AXI4LiteLSU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iAddr  = Input (UInt(ADDR_WIDTH.W))
        val iValid = Input (Bool())

        val oData  = Output(UInt(DATA_WIDTH.W))

        val pAXI4 = new AXI4LiteIO
    })

    val mAXI4LiteM = Module(new AXI4LiteM)
    mAXI4LiteM.io.iClock := clock;
    mAXI4LiteM.io.iReset := reset;
    mAXI4LiteM.io.iMode  := MODE_RD
    mAXI4LiteM.io.iAddr  := io.iAddr
    mAXI4LiteM.io.iData  := DontCare
    mAXI4LiteM.io.iMask  := DontCare
    mAXI4LiteM.io.iValid := io.iValid

    io.oData := mAXI4LiteM.io.oData

    io.pAXI4 <> mAXI4LiteM.io.pAXI4
}
