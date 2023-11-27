import chisel3._
import chisel3.util._

import cpu.common._
import cpu.core._
import cpu.blackbox._

class ITraceIO extends Bundle with ConfigIO {
    val bCTRIO = new CTRIO
    val bIDUIO = new IDUIO
    val bEXUIO = new EXUIO
    val bLSUIO = new LSUIO
    val bWBUIO = new WBUIO
}

class Top extends Module with ConfigInst {
    val io = IO(new Bundle {
        val oEndData  = Output(UInt(DATA_WIDTH.W))

        val bIFUIO    = new IFUIO
        val bGPRIO    = new GPRIO
        val bITraceIO = new ITraceIO
    })

    val mDPI = Module(new DPI)

    val mIFU = Module(new IFU)
    val mIDU = Module(new IDU)
    val mEXU = Module(new EXU)
    val mLSU = Module(new LSU)
    val mWBU = Module(new WBU)

    io.oEndData := mIDU.io.bIDUIO.oEndData

    io.bIFUIO.oPC   := mIFU.io.bIFUIO.oPC
    io.bIFUIO.oInst := mLSU.io.bLSUIO.oMemRdDataInst
    io.bGPRIO       <> mIDU.io.bGPRIO

    io.bITraceIO.bCTRIO <> mIDU.io.bCTRIO
    io.bITraceIO.bIDUIO <> mIDU.io.bIDUIO
    io.bITraceIO.bEXUIO <> mEXU.io.bEXUIO
    io.bITraceIO.bLSUIO <> mLSU.io.bLSUIO
    io.bITraceIO.bWBUIO <> mWBU.io.bWBUIO

    if (MEMS_INT.equals("DPI")) {
        mDPI.io.iClock         := clock
        mDPI.io.iReset         := reset
        mDPI.io.iMemRdEn       := mLSU.io.bLSUIO.oMemRdEn
        mDPI.io.iMemRdAddrInst := mLSU.io.bLSUIO.oMemRdAddrInst
        mDPI.io.iMemRdAddrLoad := mLSU.io.bLSUIO.oMemRdAddrLoad
        mDPI.io.iMemWrEn       := mLSU.io.bLSUIO.oMemWrEn
        mDPI.io.iMemWrAddr     := mLSU.io.bLSUIO.oMemWrAddr
        mDPI.io.iMemWrData     := mLSU.io.bLSUIO.oMemWrData
        mDPI.io.iMemWrLen      := mLSU.io.bLSUIO.oMemWrLen
    }

    val rInstName = RegNext(mIDU.io.bCTRIO.oInstName, INST_NAME_X)
    when (rInstName === INST_NAME_X && mIDU.io.bCTRIO.oStateCurr === STATE_EX) {
        assert(false.B, "Invalid instruction at 0x%x", mIFU.io.bIFUIO.oPC)
    }.elsewhen (rInstName === INST_NAME_EBREAK) {
        mDPI.io.iEbreakFlag := 1.U
    }.otherwise {
        mDPI.io.iEbreakFlag := 0.U
    }

    mIFU.io.iInstName := mIDU.io.bCTRIO.oInstName
    mIFU.io.iPCWrEn   := mIDU.io.bCTRIO.oPCWrEn
    mIFU.io.iPCWrSrc  := mIDU.io.bCTRIO.oPCWrSrc
    mIFU.io.iIRWrEn   := mIDU.io.bCTRIO.oIRWrEn
    mIFU.io.iPCNext   := mEXU.io.bEXUIO.oPCNext
    mIFU.io.iPCJump   := mEXU.io.bEXUIO.oPCJump
    mIFU.io.iALUZero  := mEXU.io.bEXUIO.oALUZero
    mIFU.io.iInst     := mLSU.io.bLSUIO.oMemRdDataInst

    mIDU.io.iPC        := mIFU.io.bIFUIO.oPC
    mIDU.io.iInst      := mIFU.io.bIFUIO.oInst
    mIDU.io.iGPRWrData := mWBU.io.bWBUIO.oGPRWrData

    mEXU.io.iPCNextEn := mIDU.io.bCTRIO.oPCNextEn
    mEXU.io.iPCJumpEn := mIDU.io.bCTRIO.oPCJumpEn
    mEXU.io.iALUType  := mIDU.io.bCTRIO.oALUType
    mEXU.io.iALURS1   := mIDU.io.bCTRIO.oALURS1
    mEXU.io.iALURS2   := mIDU.io.bCTRIO.oALURS2
    mEXU.io.iPC       := mIFU.io.bIFUIO.oPC
    mEXU.io.iRS1Data  := mIDU.io.bIDUIO.oRS1Data
    mEXU.io.iRS2Data  := mIDU.io.bIDUIO.oRS2Data
    mEXU.io.iImmData  := mIDU.io.bIDUIO.oImmData

    mLSU.io.iMemRdEn   := mIDU.io.bCTRIO.oMemRdEn
    mLSU.io.iMemRdSrc  := mIDU.io.bCTRIO.oMemRdSrc
    mLSU.io.iMemWrEn   := mIDU.io.bCTRIO.oMemWrEn
    mLSU.io.iMemByt    := mIDU.io.bCTRIO.oMemByt
    mLSU.io.iPC        := mIFU.io.bIFUIO.oPC
    mLSU.io.iALUOut    := mEXU.io.bEXUIO.oALUOut
    mLSU.io.iMemWrData := mEXU.io.bEXUIO.oMemWrData

    if (MEMS_INT.equals("DPI")) {
        mLSU.io.iMemRdDataInst := mDPI.io.oMemRdDataInst
        mLSU.io.iMemRdDataLoad := mDPI.io.oMemRdDataLoad
    }
    else {
        mLSU.io.iMemRdDataInst := DATA_ZERO
        mLSU.io.iMemRdDataLoad := DATA_ZERO
    }

    mWBU.io.iInstName := mIDU.io.bCTRIO.oInstName
    mWBU.io.iMemByt   := mIDU.io.bCTRIO.oMemByt
    mWBU.io.iGPRWrSrc := mIDU.io.bCTRIO.oGPRWrSrc
    mWBU.io.iALUOut   := mEXU.io.bEXUIO.oALUOut
    mWBU.io.iMemData  := mLSU.io.bLSUIO.oMemRdData
}
