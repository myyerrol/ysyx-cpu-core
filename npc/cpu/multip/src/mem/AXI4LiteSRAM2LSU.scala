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

    val mAXI4LiteS        = Module(new AXI4LiteS)
    val mMemDPIDirectTime = Module(new MemDPIDirectTime)

    mAXI4LiteS.io.iClock  := clock
    mAXI4LiteS.io.iReset  := reset
    mAXI4LiteS.io.iMode   := MODE_RD
    mAXI4LiteS.io.iRdData := mMemDPIDirectTime.io.oMemRdDataLoad
    mAXI4LiteS.io.iResp   := DontCare

    mMemDPIDirectTime.io.iClock         := clock
    mMemDPIDirectTime.io.iReset         := reset
    mMemDPIDirectTime.io.iMemRdEn       := io.pAXI4.ar.valid && io.pAXI4.ar.ready
    mMemDPIDirectTime.io.iMemRdAddrInst := DontCare
    mMemDPIDirectTime.io.iMemRdAddrLoad := mAXI4LiteS.io.oRdAddr
    mMemDPIDirectTime.io.iMemWrEn       := io.pAXI4.w.valid && io.pAXI4.w.ready
    mMemDPIDirectTime.io.iMemWrAddr     := mAXI4LiteS.io.oWrAddr
    mMemDPIDirectTime.io.iMemWrData     := mAXI4LiteS.io.oWrData
    mMemDPIDirectTime.io.iMemWrLen      := MuxLookup(
        mAXI4LiteS.io.oWrMask,
        8.U(BYTE_WIDTH.W),
        Seq(
            "b00000001".U(MASK_WIDTH.W) -> 1.U(BYTE_WIDTH.W),
            "b00000011".U(MASK_WIDTH.W) -> 2.U(BYTE_WIDTH.W),
            "b00001111".U(MASK_WIDTH.W) -> 4.U(BYTE_WIDTH.W),
            "b11111111".U(MASK_WIDTH.W) -> 8.U(BYTE_WIDTH.W)
        )
    )

    io.pAXI4 <> mAXI4LiteS.io.pAXI4
}
