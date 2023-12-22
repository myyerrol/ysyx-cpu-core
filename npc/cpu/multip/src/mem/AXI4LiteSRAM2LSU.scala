package cpu.mem

import chisel3._
import chisel3.util._

import cpu.blackbox._
import cpu.common._
import cpu.port._

class AXI4LiteSRAM2LSU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val pAXI4 = Flipped(new AXI4LiteIO)
    })

    val mAXI4LiteS    = Module(new AXI4LiteS)
    val mMemDPIDirect = Module(new MemDPIDirect)

    mAXI4LiteS.io.iClock := clock
    mAXI4LiteS.io.iReset := reset
    mAXI4LiteS.io.iMode  := MODE_RD
    mAXI4LiteS.io.iData  := mMemDPIDirect.io.oMemRdDataLoad
    mAXI4LiteS.io.iResp  := RESP_OKEY
    mAXI4LiteS.io.oData  := DontCare
    mAXI4LiteS.io.oMask  := DontCare

    mMemDPIDirect.io.iClock         := clock
    mMemDPIDirect.io.iReset         := reset
    mMemDPIDirect.io.iMemRdEn       := true.B
    mMemDPIDirect.io.iMemRdAddrInst := DontCare
    mMemDPIDirect.io.iMemRdAddrLoad := mAXI4LiteS.io.oAddr
    mMemDPIDirect.io.iMemWrEn       := false.B
    mMemDPIDirect.io.iMemWrAddr     := DontCare
    mMemDPIDirect.io.iMemWrData     := DontCare
    mMemDPIDirect.io.iMemWrLen      := DontCare

    io.pAXI4 <> mAXI4LiteS.io.pAXI4
}
