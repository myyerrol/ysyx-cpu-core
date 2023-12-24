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

    mAXI4LiteS.io.iClock  := clock
    mAXI4LiteS.io.iReset  := reset
    mAXI4LiteS.io.iMode   := MODE_RD
    mAXI4LiteS.io.iRdData := mMemDPIDirect.io.oMemRdDataLoad
    mAXI4LiteS.io.iResp   := DontCare

    mMemDPIDirect.io.iClock         := clock
    mMemDPIDirect.io.iReset         := reset
    mMemDPIDirect.io.iMemRdEn       := true.B
    mMemDPIDirect.io.iMemRdAddrInst := DontCare
    mMemDPIDirect.io.iMemRdAddrLoad := mAXI4LiteS.io.oRdAddr
    mMemDPIDirect.io.iMemWrEn       := io.pAXI4.w.valid && io.pAXI4.w.ready
    mMemDPIDirect.io.iMemWrAddr     := mAXI4LiteS.io.oWrAddr
    mMemDPIDirect.io.iMemWrData     := mAXI4LiteS.io.oWrData
    mMemDPIDirect.io.iMemWrLen      := MuxLookup(
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
