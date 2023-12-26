import chisel3._
import chisel3.util._

import cpu.blackbox._
import cpu.common._
import cpu.core._
import cpu.port._

class Top extends Module with ConfigInst {
    val io = IO(new Bundle {
        val oEndData = Output(UInt(DATA_WIDTH.W))

        val pIFU     = new IFUIO
        val pGPR     = new GPRIO
        val pITrace  = new ITraceIO
    })

    val mSysDPIDirect = Module(new SysDPIDirect)

    val mIFU = Module(new IFU)
    val mIDU = Module(new IDU)
    val mEXU = Module(new EXU)
    val mLSU = Module(new LSU)
    val mWBU = Module(new WBU)

    io.oEndData := mIDU.io.pIDU.oEndData

    io.pIFU.oPC   := mIFU.io.pIFU.oPC
    io.pIFU.oInst := mLSU.io.pLSU.oMemRdDataInst

    io.pGPR <> mIDU.io.pGPR

    io.pITrace.pCTR <> mIDU.io.pCTR
    io.pITrace.pIDU <> mIDU.io.pIDU
    io.pITrace.pEXU <> mEXU.io.pEXU
    io.pITrace.pLSU <> mLSU.io.pLSU
    io.pITrace.pWBU <> mWBU.io.pWBU

    val rInstName = RegNext(mIDU.io.pCTR.oInstName, INST_NAME_X)
    when (rInstName === INST_NAME_X && mIDU.io.pCTR.oStateCurr === STATE_EX) {
        assert(false.B, "Invalid instruction at 0x%x", mIFU.io.pIFU.oPC)
    }
    .elsewhen (rInstName === INST_NAME_EBREAK) {
        mSysDPIDirect.io.iEbreakFlag := 1.U
    }
    .otherwise {
        mSysDPIDirect.io.iEbreakFlag := 0.U
    }

    mIFU.io.iInstName := mIDU.io.pCTR.oInstName
    mIFU.io.iPCWrEn   := mIDU.io.pCTR.oPCWrEn
    mIFU.io.iPCWrSrc  := mIDU.io.pCTR.oPCWrSrc
    mIFU.io.iMemRdEn  := mIDU.io.pCTR.oMemRdEn
    mIFU.io.iMemRdSrc := mIDU.io.pCTR.oMemRdSrc
    mIFU.io.iIRWrEn   := mIDU.io.pCTR.oIRWrEn
    mIFU.io.iPCNext   := mEXU.io.pEXU.oPCNext
    mIFU.io.iPCJump   := mEXU.io.pEXU.oPCJump
    mIFU.io.iALUZero  := mEXU.io.pEXU.oALUZero
    mIFU.io.iInst     := mLSU.io.pLSU.oMemRdDataInst

    mIDU.io.iPC        := mIFU.io.pIFU.oPC
    mIDU.io.iInst      := mIFU.io.pIFU.oInst
    mIDU.io.iGPRWrData := mWBU.io.pWBU.oGPRWrData

    mEXU.io.iPCNextEn := mIDU.io.pCTR.oPCNextEn
    mEXU.io.iPCJumpEn := mIDU.io.pCTR.oPCJumpEn
    mEXU.io.iALUType  := mIDU.io.pCTR.oALUType
    mEXU.io.iALURS1   := mIDU.io.pCTR.oALURS1
    mEXU.io.iALURS2   := mIDU.io.pCTR.oALURS2
    mEXU.io.iPC       := mIFU.io.pIFU.oPC
    mEXU.io.iRS1Data  := mIDU.io.pIDU.oRS1Data
    mEXU.io.iRS2Data  := mIDU.io.pIDU.oRS2Data
    mEXU.io.iImmData  := mIDU.io.pIDU.oImmData

    mLSU.io.iMemRdEn   := mIDU.io.pCTR.oMemRdEn
    mLSU.io.iMemRdSrc  := mIDU.io.pCTR.oMemRdSrc
    mLSU.io.iMemWrEn   := mIDU.io.pCTR.oMemWrEn
    mLSU.io.iMemByt    := mIDU.io.pCTR.oMemByt
    mLSU.io.iPC        := mIFU.io.pIFU.oPC
    mLSU.io.iALUOut    := mEXU.io.pEXU.oALUOut
    mLSU.io.iMemWrData := mEXU.io.pEXU.oMemWrData
    mLSU.io.iState     := mIDU.io.pCTR.oStateCurr

    mWBU.io.iInstName := mIDU.io.pCTR.oInstName
    mWBU.io.iMemByt   := mIDU.io.pCTR.oMemByt
    mWBU.io.iGPRWrSrc := mIDU.io.pCTR.oGPRWrSrc
    mWBU.io.iALUOut   := mEXU.io.pEXU.oALUOut
    mWBU.io.iMemData  := mLSU.io.pLSU.oMemRdData
}
