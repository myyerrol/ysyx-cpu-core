package cpu.core

import chisel3._
import chisel3.util._

import cpu.blackbox._
import cpu.common._
import cpu.port._

class LSU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iMemRdEn       = Input(Bool())
        val iMemRdSrc      = Input(UInt(SIGS_WIDTH.W))
        val iMemWrEn       = Input(Bool())
        val iMemByt        = Input(UInt(SIGS_WIDTH.W))

        val iPC            = Input(UInt(DATA_WIDTH.W))
        val iALUOut        = Input(UInt(DATA_WIDTH.W))
        val iMemWrData     = Input(UInt(DATA_WIDTH.W))

        val iMemRdDataInst = Input(UInt(INST_WIDTH.W))
        val iMemRdDataLoad = Input(UInt(DATA_WIDTH.W))

        val pLSU           = new LSUIO
    })

    io.pLSU.oMemRdEn       := io.iMemRdEn
    io.pLSU.oMemRdAddrInst := io.iPC
    io.pLSU.oMemRdAddrLoad := io.iALUOut

    when (io.iMemWrEn) {
        io.pLSU.oMemWrEn   := true.B
        io.pLSU.oMemWrAddr := io.iALUOut
        io.pLSU.oMemWrData := io.iMemWrData
        io.pLSU.oMemWrLen  := MuxLookup(
            io.iMemByt,
            8.U(BYTE_WIDTH.W),
            Seq(
                MEM_BYT_1_U -> 1.U(BYTE_WIDTH.W),
                MEM_BYT_2_U -> 2.U(BYTE_WIDTH.W),
                MEM_BYT_4_U -> 4.U(BYTE_WIDTH.W),
                MEM_BYT_8_U -> 8.U(BYTE_WIDTH.W),
            )
        )
    }
    .otherwise {
        io.pLSU.oMemWrEn   := false.B
        io.pLSU.oMemWrAddr := DATA_ZERO
        io.pLSU.oMemWrData := DATA_ZERO
        io.pLSU.oMemWrLen  := DATA_ZERO
    }

    io.pLSU.oMemRdDataInst := DontCare
    io.pLSU.oMemRdDataLoad := DontCare

    val mMRU = Module(new MRU)
    mMRU.io.iData := DontCare

    if (MEMS_TYP.equals("DPIDirect")) {
        io.pLSU.oMemRdDataInst := io.iMemRdDataInst
        io.pLSU.oMemRdDataLoad := io.iMemRdDataLoad

        mMRU.io.iData := io.iMemRdDataLoad
    }
    else if (MEMS_TYP.equals("DPIAXI4Lite")) {
        val mAXI4LiteM = Module(new AXI4LiteM)
        mAXI4LiteM.io.iClock := clock;
        mAXI4LiteM.io.iReset := reset;
        mAXI4LiteM.io.iMode  := MODE_RD
        mAXI4LiteM.io.iAddr  := io.iPC
        mAXI4LiteM.io.iData  := DontCare
        mAXI4LiteM.io.iMask  := DontCare

        val mMemDPIDirect = Module(new MemDPIDirect)

        val mAXI4LiteS = Module(new AXI4LiteS)
        mAXI4LiteS.io.iClock := clock
        mAXI4LiteS.io.iReset := reset
        mAXI4LiteS.io.iMode  := MODE_RD
        mAXI4LiteS.io.iData  := mMemDPIDirect.io.oMemRdDataInst
        mAXI4LiteS.io.iResp  := 0.U
        mAXI4LiteS.io.oData  := DontCare
        mAXI4LiteS.io.oMask  := DontCare

        mMemDPIDirect.io.iClock         := clock
        mMemDPIDirect.io.iReset         := reset
        mMemDPIDirect.io.iMemRdEn       := true.B
        mMemDPIDirect.io.iMemRdAddrInst := mAXI4LiteS.io.oAddr
        mMemDPIDirect.io.iMemRdAddrLoad := DontCare
        mMemDPIDirect.io.iMemWrEn       := false.B
        mMemDPIDirect.io.iMemWrAddr     := DontCare
        mMemDPIDirect.io.iMemWrData     := DontCare
        mMemDPIDirect.io.iMemWrLen      := DontCare

        mAXI4LiteM.io.pAXI4 <> mAXI4LiteS.io.pAXI4

        io.pLSU.oMemRdDataInst := mAXI4LiteM.io.oData
    }
    else if (MEMS_TYP.equals("Embed")) {
        val mMemEmbed = Module(new MemEmbed)
        mMemEmbed.io.iClock := clock
        mMemEmbed.io.iReset := reset

        mMemEmbed.io.pMem.iRdEn := io.iMemRdEn
        mMemEmbed.io.pMem.iWrEn := io.iMemWrEn

        mMemEmbed.io.pMem.iAddr := DontCare

        when (io.iMemRdEn) {
            when (io.iMemRdSrc === MEM_RD_SRC_PC) {
                mMemEmbed.io.pMem.iAddr := io.iPC
                io.pLSU.oMemRdDataInst  := mMemEmbed.io.pMem.oRdData
            }
            .elsewhen (io.iMemRdSrc === MEM_RD_SRC_ALU) {
                mMemEmbed.io.pMem.iAddr := io.iALUOut
                io.pLSU.oMemRdDataLoad  := mMemEmbed.io.pMem.oRdData
            }
        }

        when (io.iMemWrEn) {
            mMemEmbed.io.pMem.iAddr   := io.iALUOut
            mMemEmbed.io.pMem.iWrData := io.iMemWrData
            mMemEmbed.io.pMem.iWrByt  := io.iMemByt
        }

        mMRU.io.iData := mMemEmbed.io.pMem.oRdData
    }

    io.pLSU.oMemRdData := mMRU.io.oData
}
