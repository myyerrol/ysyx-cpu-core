import chisel3._
import chisel3.util._

import cpu.common._
import cpu.core._
import cpu.dpi._

class ITraceIO extends Bundle with ConfigIO {
    val ctrio          = new CTRIO

    val oRS1Addr       = Output(UInt(DATA_WIDTH.W))
    val oRS2Addr       = Output(UInt(DATA_WIDTH.W))
    val oRDAddr        = Output(UInt(DATA_WIDTH.W))
    val oRS1Data       = Output(UInt(DATA_WIDTH.W))
    val oRS2Data       = Output(UInt(DATA_WIDTH.W))
    val oEndData       = Output(UInt(DATA_WIDTH.W))
    val oImmData       = Output(UInt(DATA_WIDTH.W))

    val oPCNext        = Output(UInt(DATA_WIDTH.W))
    val oPCJump        = Output(UInt(DATA_WIDTH.W))
    val oALUZero       = Output(Bool())
    val oALUOut        = Output(UInt(DATA_WIDTH.W))

    val oMemRdAddrInst = Output(UInt(DATA_WIDTH.W))
    val oMemRdAddrLoad = Output(UInt(DATA_WIDTH.W))
    val oMemRdDataInst = Output(UInt(DATA_WIDTH.W))
    val oMemRdDataLoad = Output(UInt(DATA_WIDTH.W))
    val oMemWrEn       = Output(Bool())
    val oMemWrAddr     = Output(UInt(DATA_WIDTH.W))
    val oMemWrData     = Output(UInt(DATA_WIDTH.W))
    val oMemWrLen      = Output(UInt(BYTE_WIDTH.W))
    val oMemRdData     = Output(UInt(DATA_WIDTH.W))

    val oGPRWrData     = Output(UInt(DATA_WIDTH.W))
}

class Top extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iItrace  = Input(Bool())

        val oPC      = Output(UInt(DATA_WIDTH.W))
        val oInst    = Output(UInt(DATA_WIDTH.W ))
        val oEndData = Output(UInt(DATA_WIDTH.W))

        val gprio    = new GPRIO
        val itraceio = new ITraceIO
    })

    val mDPI = Module(new DPI())
    val mIFU = Module(new IFU())
    val mIDU = Module(new IDU())
    val mEXU = Module(new EXU())
    val mLSU = Module(new LSU())
    val mWBU = Module(new WBU())

    io.oPC      := mIFU.io.oPC
    io.oInst    := mLSU.io.iMemRdDataInst
    io.oEndData := mIDU.io.oEndData
    io.gprio    <> mIDU.io.gprio

    io.itraceio.ctrio          <> mIDU.io.ctrio

    io.itraceio.oRS1Addr       := mIDU.io.oRS1Addr
    io.itraceio.oRS2Addr       := mIDU.io.oRS2Addr
    io.itraceio.oRDAddr        := mIDU.io.oRDAddr
    io.itraceio.oRS1Data       := mIDU.io.oRS1Data
    io.itraceio.oRS2Data       := mIDU.io.oRS2Data
    io.itraceio.oEndData       := mIDU.io.oEndData
    io.itraceio.oImmData       := mIDU.io.oImmData

    io.itraceio.oPCNext        := mEXU.io.oPCNext
    io.itraceio.oPCJump        := mEXU.io.oPCJump
    io.itraceio.oALUZero       := mEXU.io.oALUZero
    io.itraceio.oALUOut        := mEXU.io.oALUOut

    io.itraceio.oMemRdAddrInst := mLSU.io.oMemRdAddrInst
    io.itraceio.oMemRdAddrLoad := mLSU.io.oMemRdAddrLoad
    io.itraceio.oMemRdDataInst := mLSU.io.iMemRdDataInst
    io.itraceio.oMemRdDataLoad := mLSU.io.iMemRdDataLoad
    io.itraceio.oMemWrEn       := mLSU.io.oMemWrEn
    io.itraceio.oMemWrAddr     := mLSU.io.oMemWrAddr
    io.itraceio.oMemWrData     := mLSU.io.oMemWrData
    io.itraceio.oMemWrLen      := mLSU.io.oMemWrLen
    io.itraceio.oMemRdData     := mLSU.io.oMemRdData

    io.itraceio.oGPRWrData     := mWBU.io.oGPRWrData

    mDPI.io.iMemRdAddrInst := mLSU.io.oMemRdAddrInst
    mDPI.io.iMemRdAddrLoad := mLSU.io.oMemRdAddrLoad
    mDPI.io.iMemWrEn       := mLSU.io.oMemWrEn
    mDPI.io.iMemWrAddr     := mLSU.io.oMemWrAddr
    mDPI.io.iMemWrData     := mLSU.io.oMemWrData
    mDPI.io.iMemWrLen      := mLSU.io.oMemWrLen

    val rInstName = RegNext(mIDU.io.ctrio.oInstName, INST_NAME_X)
    when (rInstName === INST_NAME_X && mIDU.io.ctrio.oStateCurr === 2.U) {
        assert(false.B, "Invalid instruction at 0x%x", mIFU.io.oPC)
    }.elsewhen (rInstName === INST_NAME_EBREAK) {
        mDPI.io.iEbreakFlag := 1.U
    }.otherwise {
        mDPI.io.iEbreakFlag := 0.U
    }

    mIFU.io.iInstName  := mIDU.io.ctrio.oInstName
    mIFU.io.iPCWrEn    := mIDU.io.ctrio.oPCWrEn
    mIFU.io.iPCWrSrc   := mIDU.io.ctrio.oPCWrSrc
    mIFU.io.iIRWrEn    := mIDU.io.ctrio.oIRWrEn
    mIFU.io.iPCNext    := mEXU.io.oPCNext
    mIFU.io.iPCJump    := mEXU.io.oPCJump
    mIFU.io.iALUZero   := mEXU.io.oALUZero
    mIFU.io.iInst      := mLSU.io.iMemRdDataInst

    mIDU.io.iPC        := mIFU.io.oPC
    mIDU.io.iInst      := mIFU.io.oInst
    mIDU.io.iGPRWrData := mWBU.io.oGPRWrData

    mEXU.io.iPCNextEn := mIDU.io.ctrio.oPCNextEn
    mEXU.io.iPCJumpEn := mIDU.io.ctrio.oPCJumpEn
    mEXU.io.iALUType  := mIDU.io.ctrio.oALUType
    mEXU.io.iALURS1   := mIDU.io.ctrio.oALURS1
    mEXU.io.iALURS2   := mIDU.io.ctrio.oALURS2
    mEXU.io.iPC       := mIFU.io.oPC
    mEXU.io.iRS1Data  := mIDU.io.oRS1Data
    mEXU.io.iRS2Data  := mIDU.io.oRS2Data
    mEXU.io.iImmData  := mIDU.io.oImmData

    mLSU.io.iPC        := mIFU.io.oPC
    mLSU.io.iALUOut    := mEXU.io.oALUOut
    mLSU.io.iMemWrEn   := mIDU.io.ctrio.oMemWrEn
    mLSU.io.iMemByt    := mIDU.io.ctrio.oMemByt
    mLSU.io.iMemWrData := mEXU.io.oMemWrData

    mLSU.io.iMemRdDataInst := mDPI.io.oMemRdDataInst
    mLSU.io.iMemRdDataLoad := mDPI.io.oMemRdDataLoad

    mWBU.io.iInstName := mIDU.io.ctrio.oInstName
    mWBU.io.iMemByt   := mIDU.io.ctrio.oMemByt
    mWBU.io.iGPRWrSrc := mIDU.io.ctrio.oGPRWrSrc
    mWBU.io.iALUOut   := mEXU.io.oALUOut
    mWBU.io.iMemData  := mLSU.io.oMemRdData
}
