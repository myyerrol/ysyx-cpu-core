package cpu.mem

import chisel3._
import chisel3.util._

import cpu.blackbox._
import cpu.common._
import cpu.port._

class AXI4LiteSRAM2IFU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val pAXI4 = Flipped(new AXI4LiteIO)
    })

    val mAXI4LiteS        = Module(new AXI4LiteS)
    val mMemDPIDirectTime = Module(new MemDPIDirectTime)

    mAXI4LiteS.io.iClock  := clock
    mAXI4LiteS.io.iReset  := reset
    mAXI4LiteS.io.iMode   := MODE_RD
    mAXI4LiteS.io.iRdData := mMemDPIDirectTime.io.oMemRdDataInst
    mAXI4LiteS.io.iResp   := DontCare
    mAXI4LiteS.io.oWrData := DontCare
    mAXI4LiteS.io.oWrMask := DontCare

    mMemDPIDirectTime.io.iClock         := clock
    mMemDPIDirectTime.io.iReset         := reset
    mMemDPIDirectTime.io.iMemRdEn       := io.pAXI4.ar.valid && io.pAXI4.ar.ready
    mMemDPIDirectTime.io.iMemRdAddrInst := mAXI4LiteS.io.oRdAddr
    mMemDPIDirectTime.io.iMemRdAddrLoad := DontCare
    mMemDPIDirectTime.io.iMemWrEn       := false.B
    mMemDPIDirectTime.io.iMemWrAddr     := DontCare
    mMemDPIDirectTime.io.iMemWrData     := DontCare
    mMemDPIDirectTime.io.iMemWrLen      := DontCare

    io.pAXI4 <> mAXI4LiteS.io.pAXI4
}
