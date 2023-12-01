import chisel3._
import chisel3.util._

import cpu.blackbox._
import cpu.common._
import cpu.core._

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

    val mMemDPIDirect = Module(new MemDPIDirect)
    val mSysDPIDirect = Module(new SysDPIDirect)

    val mIFU = Module(new IFU)
    val mIDU = Module(new IDU)
    val mEXU = Module(new EXU)
    val mLSU = Module(new LSU)
    val mWBU = Module(new WBU)

    mIFU.io.bIFUAXIMasterARIO <> DontCare
    mIFU.io.bIFUAXIMasterRIO  <> DontCare

    io.oEndData := mIDU.io.bIDUIO.oEndData

    io.bIFUIO.oPC := mIFU.io.bIFUIO.oPC
    if (MEMS_TYP.equals("DPIDirect") || MEMS_TYP.equals("Embed")) {
        io.bIFUIO.oInst := mLSU.io.bLSUIO.oMemRdDataInst
    }
    else {
        when (mIFU.io.oState === 1.U && mIFU.io.bIFUAXIMasterRIO.rresp === 0.U) {
            io.bIFUIO.oInst := mIFU.io.bIFUAXIMasterRIO.rdata
        }
        .otherwise {
            io.bIFUIO.oInst := DATA_ZERO
        }
    }

    io.bGPRIO <> mIDU.io.bGPRIO

    io.bITraceIO.bCTRIO <> mIDU.io.bCTRIO
    io.bITraceIO.bIDUIO <> mIDU.io.bIDUIO
    io.bITraceIO.bEXUIO <> mEXU.io.bEXUIO
    io.bITraceIO.bLSUIO <> mLSU.io.bLSUIO
    io.bITraceIO.bWBUIO <> mWBU.io.bWBUIO

    if (MEMS_TYP.equals("DPIDirect")) {
        mMemDPIDirect.io.iClock         := clock
        mMemDPIDirect.io.iReset         := reset
        mMemDPIDirect.io.iMemRdEn       := mLSU.io.bLSUIO.oMemRdEn
        mMemDPIDirect.io.iMemRdAddrInst := mLSU.io.bLSUIO.oMemRdAddrInst
        mMemDPIDirect.io.iMemRdAddrLoad := mLSU.io.bLSUIO.oMemRdAddrLoad
        mMemDPIDirect.io.iMemWrEn       := mLSU.io.bLSUIO.oMemWrEn
        mMemDPIDirect.io.iMemWrAddr     := mLSU.io.bLSUIO.oMemWrAddr
        mMemDPIDirect.io.iMemWrData     := mLSU.io.bLSUIO.oMemWrData
        mMemDPIDirect.io.iMemWrLen      := mLSU.io.bLSUIO.oMemWrLen
    }
    else if (MEMS_TYP.equals("DPIAXI4Lite")) {
        val mMemDPIAXI4LiteLFU = Module(new MemDPIAXI4LiteLFU)
        mMemDPIAXI4LiteLFU.io.iClock := clock
        mMemDPIAXI4LiteLFU.io.iReset := reset
        mMemDPIAXI4LiteLFU.io.bIFUAXISlaveARIO <> mIFU.io.bIFUAXIMasterARIO
        mMemDPIAXI4LiteLFU.io.bIFUAXISlaveRIO  <> mIFU.io.bIFUAXIMasterRIO
    }

    val rInstName = RegNext(mIDU.io.bCTRIO.oInstName, INST_NAME_X)
    when (rInstName === INST_NAME_X && mIDU.io.bCTRIO.oStateCurr === STATE_EX) {
        assert(false.B, "Invalid instruction at 0x%x", mIFU.io.bIFUIO.oPC)
    }
    .elsewhen (rInstName === INST_NAME_EBREAK) {
        mSysDPIDirect.io.iEbreakFlag := 1.U
    }
    .otherwise {
        mSysDPIDirect.io.iEbreakFlag := 0.U
    }

    mIFU.io.iInstName := mIDU.io.bCTRIO.oInstName
    mIFU.io.iPCWrEn   := mIDU.io.bCTRIO.oPCWrEn
    mIFU.io.iPCWrSrc  := mIDU.io.bCTRIO.oPCWrSrc
    mIFU.io.iMemRdEn  := mIDU.io.bCTRIO.oMemRdEn
    mIFU.io.iMemRdSrc := mIDU.io.bCTRIO.oMemRdSrc
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

    if (MEMS_TYP.equals("DPIDirect")) {
        mLSU.io.iMemRdDataInst := mMemDPIDirect.io.oMemRdDataInst
        mLSU.io.iMemRdDataLoad := mMemDPIDirect.io.oMemRdDataLoad
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
