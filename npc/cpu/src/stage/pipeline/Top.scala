package cpu.stage.pipeline

import chisel3._
import chisel3.util._

import cpu.blackbox._
import cpu.common._
import cpu.port._

class Top extends Module with ConfigInst {
    val io = IO(new Bundle {
        val oEndData = Output(UInt(DATA_WIDTH.W))

        val pIFU     = new IFUIO
        val pGPR     = new GPRIO
        val pITrace  = new ITraceIO

        val oPC1 = Output(UInt(ADDR_WIDTH.W))
        val oPC2 = Output(UInt(ADDR_WIDTH.W))
        val oPC3 = Output(UInt(ADDR_WIDTH.W))
        val oPC4 = Output(UInt(ADDR_WIDTH.W))
    });

    val mSysDPIDirect = Module(new SysDPIDirect)

    val mIFU = Module(new IFU)
    val mIDU = Module(new IDU)
    val mEXU = Module(new EXU)
    val mLSU = Module(new LSU)
    val mWBU = Module(new WBU)

    val mIFU2IDU = Module(new IFU2IDU)
    val mIDU2EXU = Module(new IDU2EXU)
    val mEXU2LSU = Module(new EXU2LSU)
    val mLSU2WBU = Module(new LSU2WBU)

    io.oPC1 := mIFU2IDU.io.pRegPipe.oPC
    io.oPC2 := mIDU2EXU.io.pRegPipe.oPC
    io.oPC3 := mEXU2LSU.io.pRegPipe.oPC
    io.oPC4 := mLSU2WBU.io.pRegPipe.oPC

    io.oEndData := 0.U

    io.pIFU.oPC   := mIFU.io.pIFU.oPC
    io.pIFU.oInst := mLSU.io.pLSU.oMemRdDataInst

    io.pGPR <> mIDU.io.pGPR

    io.pITrace.pCTR <> mIDU.io.pCTR
    io.pITrace.pIDU <> mIDU.io.pIDU
    io.pITrace.pEXU <> mEXU.io.pEXU
    io.pITrace.pLSU <> mLSU.io.pLSU
    io.pITrace.pWBU <> mWBU.io.pWBU

    mIFU.io.iJmpEn := false.B
    mIFU.io.iJmpPC := ADDR_INIT
    mIFU.io.iInst  := mLSU.io.pLSU.oMemRdDataInst

    mIFU2IDU.io.pRegPipe.iEn    := true.B
    mIFU2IDU.io.pRegPipe.iValid := true.B
    mIFU2IDU.io.pRegPipe.iReady := true.B
    mIFU2IDU.io.pRegPipe.iPC    := mIFU.io.pIFU.oPC
    mIFU2IDU.io.pRegPipe.iInst  := mIFU.io.pIFU.oInst

    mIDU.io.iPC        := mIFU2IDU.io.pRegPipe.oPC
    mIDU.io.iInst      := mIFU2IDU.io.pRegPipe.oInst
    mIDU.io.iGPRWrData := mWBU.io.pWBU.oGPRWrData

    mIDU2EXU.io.pRegPipe.iEn    := true.B
    mIDU2EXU.io.pRegPipe.iValid := true.B
    mIDU2EXU.io.pRegPipe.iReady := true.B
    mIDU2EXU.io.pRegPipe.iPC    := mIFU2IDU.io.pRegPipe.oPC
    mIDU2EXU.io.pRegPipe.iInst  := mIFU2IDU.io.pRegPipe.oInst

    mIDU2EXU.io.iInstName := mIDU.io.pCTR.oInstName
    mIDU2EXU.io.iMemWrEn  := mIDU.io.pCTR.oMemWrEn
    mIDU2EXU.io.iMemByt   := mIDU.io.pCTR.oMemByt
    mIDU2EXU.io.iGPRWrEn  := mIDU.io.pCTR.oGPRWrEn
    mIDU2EXU.io.iGPRWrSrc := mIDU.io.pCTR.oGPRWrSrc
    mIDU2EXU.io.iALUType  := mIDU.io.pCTR.oALUType
    mIDU2EXU.io.iALURS1   := mIDU.io.pCTR.oALURS1
    mIDU2EXU.io.iALURS2   := mIDU.io.pCTR.oALURS2
    mIDU2EXU.io.iRS1Data  := mIDU.io.pIDU.oRS1Data
    mIDU2EXU.io.iRS2Data  := mIDU.io.pIDU.oRS2Data
    mIDU2EXU.io.iImmData  := mIDU.io.pIDU.oImmData

    mEXU.io.iALUType := mIDU2EXU.io.pCTR.oALUType
    mEXU.io.iALURS1  := mIDU2EXU.io.pCTR.oALURS1
    mEXU.io.iALURS2  := mIDU2EXU.io.pCTR.oALURS2
    mEXU.io.iPC      := mIDU2EXU.io.pRegPipe.oPC
    mEXU.io.iRS1Data := mIDU2EXU.io.pIDU.oRS1Data
    mEXU.io.iRS2Data := mIDU2EXU.io.pIDU.oRS2Data
    mEXU.io.iImmData := mIDU2EXU.io.pIDU.oImmData

    mEXU2LSU.io.pRegPipe.iEn    := true.B
    mEXU2LSU.io.pRegPipe.iValid := true.B
    mEXU2LSU.io.pRegPipe.iReady := true.B
    mEXU2LSU.io.pRegPipe.iPC    := mIDU2EXU.io.pRegPipe.oPC
    mEXU2LSU.io.pRegPipe.iInst  := mIDU2EXU.io.pRegPipe.oInst
    mEXU2LSU.io.iInstName       := mIDU2EXU.io.pCTR.oInstName
    mEXU2LSU.io.iMemWrEn        := mIDU2EXU.io.pCTR.oMemWrEn
    mEXU2LSU.io.iMemByt         := mIDU2EXU.io.pCTR.oMemByt
    mEXU2LSU.io.iGPRWrEn        := mIDU2EXU.io.pCTR.oGPRWrEn
    mEXU2LSU.io.iGPRWrSrc       := mIDU2EXU.io.pCTR.oGPRWrSrc
    mEXU2LSU.io.iALUType        := mIDU2EXU.io.pCTR.oALUType
    mEXU2LSU.io.iALURS1         := mIDU2EXU.io.pCTR.oALURS1
    mEXU2LSU.io.iALURS2         := mIDU2EXU.io.pCTR.oALURS2
    mEXU2LSU.io.iALUOut         := mEXU.io.pEXU.oALUOut
    mEXU2LSU.io.iMemWrData      := mEXU.io.pEXU.oMemWrData

    mLSU.io.iMemRdEn   := true.B
    mLSU.io.iMemWrEn   := mEXU2LSU.io.pCTR.oMemWrEn
    mLSU.io.iMemByt    := mEXU2LSU.io.pCTR.oMemByt
    mLSU.io.iPC        := mIFU.io.pIFU.oPC
    mLSU.io.iALUOut    := mEXU2LSU.io.pEXU.oALUOut
    mLSU.io.iMemWrData := mEXU2LSU.io.pEXU.oMemWrData

    mLSU2WBU.io.pRegPipe.iEn    := true.B
    mLSU2WBU.io.pRegPipe.iValid := true.B
    mLSU2WBU.io.pRegPipe.iReady := true.B
    mLSU2WBU.io.pRegPipe.iPC    := mEXU2LSU.io.pRegPipe.oPC
    mLSU2WBU.io.pRegPipe.iInst  := mEXU2LSU.io.pRegPipe.oInst
    mLSU2WBU.io.iInstName       := mEXU2LSU.io.pCTR.oInstName
    mLSU2WBU.io.iMemWrEn        := mEXU2LSU.io.pCTR.oMemWrEn
    mLSU2WBU.io.iMemByt         := mEXU2LSU.io.pCTR.oMemByt
    mLSU2WBU.io.iGPRWrEn        := mEXU2LSU.io.pCTR.oGPRWrEn
    mLSU2WBU.io.iGPRWrSrc       := mEXU2LSU.io.pCTR.oGPRWrSrc
    mLSU2WBU.io.iALUType        := mEXU2LSU.io.pCTR.oALUType
    mLSU2WBU.io.iALURS1         := mEXU2LSU.io.pCTR.oALURS1
    mLSU2WBU.io.iALURS2         := mEXU2LSU.io.pCTR.oALURS2
    mLSU2WBU.io.iALUOut         := mEXU2LSU.io.pEXU.oALUOut
    mLSU2WBU.io.iMemRdData      := mLSU.io.pLSU.oMemRdData

    mWBU.io.iInstName := mLSU2WBU.io.pCTR.oInstName
    mWBU.io.iMemByt   := mLSU2WBU.io.pCTR.oMemByt
    mWBU.io.iGPRWrSrc := mLSU2WBU.io.pCTR.oGPRWrSrc
    mWBU.io.iALUOut   := mLSU2WBU.io.pEXU.oALUOut
    mWBU.io.iMemData  := mLSU2WBU.io.pLSU.oMemRdData
}