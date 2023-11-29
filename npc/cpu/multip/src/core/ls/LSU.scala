package cpu.core

import chisel3._
import chisel3.util._

import cpu.blackbox._
import cpu.common._

class LSUIO extends Bundle with ConfigIO {
    val oMemRdEn       = Output(Bool())
    val oMemRdAddrInst = Output(UInt(DATA_WIDTH.W))
    val oMemRdAddrLoad = Output(UInt(DATA_WIDTH.W))
    val oMemWrEn       = Output(Bool())
    val oMemWrAddr     = Output(UInt(DATA_WIDTH.W))
    val oMemWrData     = Output(UInt(DATA_WIDTH.W))
    val oMemWrLen      = Output(UInt(BYTE_WIDTH.W))

    val oMemRdDataInst = Output(UInt(DATA_WIDTH.W))
    val oMemRdDataLoad = Output(UInt(DATA_WIDTH.W))
    val oMemRdData     = Output(UInt(DATA_WIDTH.W))
}

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

        val bLSUIO         = new LSUIO
    })

    io.bLSUIO.oMemRdEn       := io.iMemRdEn
    io.bLSUIO.oMemRdAddrInst := io.iPC
    io.bLSUIO.oMemRdAddrLoad := io.iALUOut

    when (io.iMemWrEn) {
        io.bLSUIO.oMemWrEn   := true.B
        io.bLSUIO.oMemWrAddr := io.iALUOut
        io.bLSUIO.oMemWrData := io.iMemWrData
        io.bLSUIO.oMemWrLen  := MuxLookup(
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
        io.bLSUIO.oMemWrEn   := false.B
        io.bLSUIO.oMemWrAddr := DATA_ZERO
        io.bLSUIO.oMemWrData := DATA_ZERO
        io.bLSUIO.oMemWrLen  := DATA_ZERO
    }

    val mMRU = Module(new MRU)
    if (MEMS_TYP.equals("DPIDirect")) {
        io.bLSUIO.oMemRdDataInst := io.iMemRdDataInst
        io.bLSUIO.oMemRdDataLoad := io.iMemRdDataLoad

        mMRU.io.iData := io.iMemRdDataLoad
    }
    else if (MEMS_TYP.equals("DPIAXI4Lite")) {

    }
    else if (MEMS_TYP.equals("Embed")) {
        val mMemEmbed = Module(new MemEmbed)
        mMemEmbed.io.iClock := clock
        mMemEmbed.io.iReset := reset

        mMemEmbed.io.bMemPortDualIO.iRdEn := io.iMemRdEn
        mMemEmbed.io.bMemPortDualIO.iWrEn := io.iMemWrEn

        mMemEmbed.io.bMemPortDualIO.iAddr := DontCare
        io.bLSUIO.oMemRdDataInst          := DontCare
        io.bLSUIO.oMemRdDataLoad          := DontCare

        when (io.iMemRdEn) {
            when (io.iMemRdSrc === MEM_RD_SRC_PC) {
                mMemEmbed.io.bMemPortDualIO.iAddr := io.iPC
                io.bLSUIO.oMemRdDataInst          := mMemEmbed.io.bMemPortDualIO.oRdData
            }
            .elsewhen (io.iMemRdSrc === MEM_RD_SRC_ALU) {
                mMemEmbed.io.bMemPortDualIO.iAddr := io.iALUOut
                io.bLSUIO.oMemRdDataInst          := mMemEmbed.io.bMemPortDualIO.oRdData
            }
        }

        when (io.iMemWrEn) {
            mMemEmbed.io.bMemPortDualIO.iAddr   := io.iALUOut
            mMemEmbed.io.bMemPortDualIO.iWrData := io.iMemWrData
            mMemEmbed.io.bMemPortDualIO.iWrByt  := io.iMemByt
        }

        mMRU.io.iData := mMemEmbed.io.bMemPortDualIO.oRdData
    }

    io.bLSUIO.oMemRdData := mMRU.io.oData
}
