package cpu.core

import chisel3._
import chisel3.util._

import cpu.blackbox._
import cpu.common._
import cpu.mem._
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
        val mMemDPIDirectComob = Module(new MemDPIDirectComb)
        mMemDPIDirectComob.io.iClock         := clock
        mMemDPIDirectComob.io.iReset         := reset
        mMemDPIDirectComob.io.iMemRdEn       := io.pLSU.oMemRdEn
        mMemDPIDirectComob.io.iMemRdAddrInst := io.pLSU.oMemRdAddrInst
        mMemDPIDirectComob.io.iMemRdAddrLoad := io.pLSU.oMemRdAddrLoad
        mMemDPIDirectComob.io.iMemWrEn       := io.pLSU.oMemWrEn
        mMemDPIDirectComob.io.iMemWrAddr     := io.pLSU.oMemWrAddr
        mMemDPIDirectComob.io.iMemWrData     := io.pLSU.oMemWrData
        mMemDPIDirectComob.io.iMemWrLen      := io.pLSU.oMemWrLen

        io.pLSU.oMemRdDataInst := mMemDPIDirectComob.io.oMemRdDataInst
        io.pLSU.oMemRdDataLoad := mMemDPIDirectComob.io.oMemRdDataLoad

        mMRU.io.iData := mMemDPIDirectComob.io.oMemRdDataLoad
    }
    else if (MEMS_TYP.equals("DPIAXI4Lite")) {
        val mAXI4LiteIFU  = Module(new AXI4LiteIFU)
        mAXI4LiteIFU.io.iRdValid := (io.iMemRdEn &&
                                     io.iMemRdSrc === MEM_RD_SRC_PC)
        mAXI4LiteIFU.io.iRdAddr  :=  io.iPC

        val mAXI4LiteSRAM2IFU = Module(new AXI4LiteSRAM2IFU)
        mAXI4LiteIFU.io.pAXI4 <> mAXI4LiteSRAM2IFU.io.pAXI4

        io.pLSU.oMemRdDataInst := mAXI4LiteIFU.io.oRdData

        // --------------------------------------------------------------------
        // val mAXI4LiteLSU = Module(new AXI4LiteLSU)
        // mAXI4LiteLSU.io.iRdValid := (io.iMemRdEn &&
        //                              io.iMemRdSrc === MEM_RD_SRC_ALU)
        // mAXI4LiteLSU.io.iRdAddr  :=  io.iALUOut
        // mAXI4LiteLSU.io.iWrValid :=  io.iMemWrEn
        // mAXI4LiteLSU.io.iWrAddr  :=  io.iALUOut
        // mAXI4LiteLSU.io.iWrData  :=  io.iMemWrData
        // mAXI4LiteLSU.io.iWrMask  :=  MuxLookup(
        //     io.iMemByt,
        //     "b11111111".U(MASK_WIDTH.W),
        //     Seq(
        //         MEM_BYT_1_U -> "b00000001".U(MASK_WIDTH.W),
        //         MEM_BYT_2_U -> "b00000011".U(MASK_WIDTH.W),
        //         MEM_BYT_4_U -> "b00001111".U(MASK_WIDTH.W),
        //         MEM_BYT_8_U -> "b11111111".U(MASK_WIDTH.W)
        //     )
        // )

        // val mAXI4LiteSRAM2LSU = Module(new AXI4LiteSRAM2LSU)
        // mAXI4LiteLSU.io.pAXI4 <> mAXI4LiteSRAM2LSU.io.pAXI4

        // io.pLSU.oMemRdDataLoad := mAXI4LiteLSU.io.oRdData
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
    else {
        io.pLSU.oMemRdDataInst := DATA_ZERO
        io.pLSU.oMemRdDataLoad := DATA_ZERO

        mMRU.io.iData := DATA_ZERO
    }

    io.pLSU.oMemRdData := mMRU.io.oData
}
