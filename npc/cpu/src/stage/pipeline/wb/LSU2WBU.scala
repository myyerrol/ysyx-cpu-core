package cpu.stage.pipeline

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.port._

class LSU2WBU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val pRegPipe = new RegPipeIO

        val iInstName = Input(UInt(SIGS_WIDTH.W))
        val iMemWrEn  = Input(Bool())
        val iMemByt   = Input(UInt(SIGS_WIDTH.W))
        val iGPRWrEn  = Input(Bool())
        val iGPRWrSrc = Input(UInt(SIGS_WIDTH.W))
        val iALUType  = Input(UInt(SIGS_WIDTH.W))
        val iALURS1   = Input(UInt(SIGS_WIDTH.W))
        val iALURS2   = Input(UInt(SIGS_WIDTH.W))

        val iALUOut    = Input(UInt(DATA_WIDTH.W))
        val iMemRdData = Input(UInt(DATA_WIDTH.W))

        val pCTR     = new CTRIO
        val pEXU     = new EXUIO
        val pLSU     = new LSUIO
    })

    val rValid = RegEnable(io.pRegPipe.iValid, false.B,   io.pRegPipe.iEn)
    val rReady = RegEnable(io.pRegPipe.iReady, false.B,   io.pRegPipe.iEn)
    val rPC    = RegEnable(io.pRegPipe.iPC,    ADDR_ZERO, io.pRegPipe.iEn)
    val rInst  = RegEnable(io.pRegPipe.iInst,  INST_ZERO, io.pRegPipe.iEn)

    io.pRegPipe.oValid := rValid
    io.pRegPipe.oReady := rReady
    io.pRegPipe.oPC    := rPC
    io.pRegPipe.oInst  := rInst

    io.pCTR <> DontCare
    io.pEXU <> DontCare
    io.pLSU <> DontCare

    val rInstName = RegEnable(io.iInstName, INST_NAME_X,  io.pRegPipe.iEn)
    val rMemWrEn  = RegEnable(io.iMemWrEn,  false.B,      io.pRegPipe.iEn)
    val rMemByt   = RegEnable(io.iMemByt,   MEM_BYT_X,    io.pRegPipe.iEn)
    val rGPRWrEn  = RegEnable(io.iGPRWrEn,  false.B,      io.pRegPipe.iEn)
    val rGPRWrSrc = RegEnable(io.iGPRWrSrc, GPR_WR_SRC_X, io.pRegPipe.iEn)
    val rALUType  = RegEnable(io.iALUType,  ALU_TYPE_X,   io.pRegPipe.iEn)
    val rALURS1   = RegEnable(io.iALURS1,   ALU_RS1_X,    io.pRegPipe.iEn)
    val rALURS2   = RegEnable(io.iALURS2,   ALU_RS2_X,    io.pRegPipe.iEn)

    io.pCTR.oInstName  := rInstName
    io.pCTR.oMemRdEn   := true.B
    io.pCTR.oMemWrEn   := rMemWrEn
    io.pCTR.oMemByt    := rMemByt
    io.pCTR.oGPRWrEn   := rGPRWrEn
    io.pCTR.oGPRWrSrc  := rGPRWrSrc
    io.pCTR.oALUType   := rALUType
    io.pCTR.oALURS1    := rALURS1
    io.pCTR.oALURS2    := rALURS2

    val rALUOut    = RegEnable(io.iALUOut,    DATA_ZERO, io.pRegPipe.iEn)
    val rMemRdData = RegEnable(io.iMemRdData, DATA_ZERO, io.pRegPipe.iEn)

    io.pEXU.oALUOut    := rALUOut
    io.pLSU.oMemRdData := rMemRdData
}
