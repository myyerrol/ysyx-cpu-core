import chisel3._
import chisel3.util._

import cpu.common._
import cpu.core._
import cpu.dpi._

class ITraceIO extends Bundle with ConfigIO {
    val ctrio = new CTRIO
    val iduio = new IDUIO
    val exuio = new EXUIO
    val lsuio = new LSUIO
    val wbuio = new WBUIO
}

class Top extends Module with ConfigInst {
    val io = IO(new Bundle {
        val oEndData = Output(UInt(DATA_WIDTH.W))

        val ifuio    = new IFUIO
        val gprio    = new GPRIO
        val itraceio = new ITraceIO
    })

    val mDPI = Module(new DPI)
    val mIFU = Module(new IFU)
    val mIDU = Module(new IDU)
    val mEXU = Module(new EXU)
    val mLSU = Module(new LSU)
    val mWBU = Module(new WBU)

    io.oEndData := mIDU.io.iduio.oEndData

    io.ifuio.oPC   := mIFU.io.ifuio.oPC
    io.ifuio.oInst := mLSU.io.lsuio.oMemRdDataInst
    io.gprio       <> mIDU.io.gprio

    io.itraceio.ctrio <> mIDU.io.ctrio
    io.itraceio.iduio <> mIDU.io.iduio
    io.itraceio.exuio <> mEXU.io.exuio
    io.itraceio.lsuio <> mLSU.io.lsuio
    io.itraceio.wbuio <> mWBU.io.wbuio

    // mDPI.io.iClock         := clock
    // mDPI.io.iReset         := reset
    // mDPI.io.iMemRdEn       := mLSU.io.lsuio.oMemRdEn
    // mDPI.io.iMemRdAddrInst := mLSU.io.lsuio.oMemRdAddrInst
    // mDPI.io.iMemRdAddrLoad := mLSU.io.lsuio.oMemRdAddrLoad
    // mDPI.io.iMemWrEn       := mLSU.io.lsuio.oMemWrEn
    // mDPI.io.iMemWrAddr     := mLSU.io.lsuio.oMemWrAddr
    // mDPI.io.iMemWrData     := mLSU.io.lsuio.oMemWrData
    // mDPI.io.iMemWrLen      := mLSU.io.lsuio.oMemWrLen

    val rInstName = RegNext(mIDU.io.ctrio.oInstName, INST_NAME_X)
    when (rInstName === INST_NAME_X && mIDU.io.ctrio.oStateCurr === STATE_EX) {
        assert(false.B, "Invalid instruction at 0x%x", mIFU.io.ifuio.oPC)
    }.elsewhen (rInstName === INST_NAME_EBREAK) {
        mDPI.io.iEbreakFlag := 1.U
    }.otherwise {
        mDPI.io.iEbreakFlag := 0.U
    }

    mIFU.io.iInstName  := mIDU.io.ctrio.oInstName
    mIFU.io.iPCWrEn    := mIDU.io.ctrio.oPCWrEn
    mIFU.io.iPCWrSrc   := mIDU.io.ctrio.oPCWrSrc
    mIFU.io.iIRWrEn    := mIDU.io.ctrio.oIRWrEn
    mIFU.io.iPCNext    := mEXU.io.exuio.oPCNext
    mIFU.io.iPCJump    := mEXU.io.exuio.oPCJump
    mIFU.io.iALUZero   := mEXU.io.exuio.oALUZero
    mIFU.io.iInst      := mLSU.io.lsuio.oMemRdDataInst

    mIDU.io.iPC        := mIFU.io.ifuio.oPC
    mIDU.io.iInst      := mIFU.io.ifuio.oInst
    mIDU.io.iGPRWrData := mWBU.io.wbuio.oGPRWrData

    mEXU.io.iPCNextEn := mIDU.io.ctrio.oPCNextEn
    mEXU.io.iPCJumpEn := mIDU.io.ctrio.oPCJumpEn
    mEXU.io.iALUType  := mIDU.io.ctrio.oALUType
    mEXU.io.iALURS1   := mIDU.io.ctrio.oALURS1
    mEXU.io.iALURS2   := mIDU.io.ctrio.oALURS2
    mEXU.io.iPC       := mIFU.io.ifuio.oPC
    mEXU.io.iRS1Data  := mIDU.io.iduio.oRS1Data
    mEXU.io.iRS2Data  := mIDU.io.iduio.oRS2Data
    mEXU.io.iImmData  := mIDU.io.iduio.oImmData

    mLSU.io.iMemRdEn   := mIDU.io.ctrio.oMemRdEn
    mLSU.io.iMemRdSrc  := mIDU.io.ctrio.oMemRdSrc
    mLSU.io.iMemWrEn   := mIDU.io.ctrio.oMemWrEn
    mLSU.io.iMemByt    := mIDU.io.ctrio.oMemByt
    mLSU.io.iPC        := mIFU.io.ifuio.oPC
    mLSU.io.iALUOut    := mEXU.io.exuio.oALUOut
    mLSU.io.iMemWrData := mEXU.io.exuio.oMemWrData

    // mLSU.io.iMemRdDataInst := mDPI.io.oMemRdDataInst
    // mLSU.io.iMemRdDataLoad := mDPI.io.oMemRdDataLoad
    mLSU.io.iMemRdDataInst := DontCare
    mLSU.io.iMemRdDataLoad := DontCare

    mWBU.io.iInstName := mIDU.io.ctrio.oInstName
    mWBU.io.iMemByt   := mIDU.io.ctrio.oMemByt
    mWBU.io.iGPRWrSrc := mIDU.io.ctrio.oGPRWrSrc
    mWBU.io.iALUOut   := mEXU.io.exuio.oALUOut
    mWBU.io.iMemData  := mLSU.io.lsuio.oMemRdData
}
