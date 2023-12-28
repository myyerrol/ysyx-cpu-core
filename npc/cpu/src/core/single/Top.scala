package cpu.core.single

import chisel3._
import chisel3.util._

import cpu.blackbox._
import cpu.common._
import cpu.core.single._
import cpu.port._

class Top extends Module with ConfigInst {
    val io = IO(new Bundle {
        val oEndData  = Output(UInt(DATA_WIDTH.W))
        val oInstCall = Output(Bool())
        val oInstRet  = Output(Bool())

        val pIFU      = new IFUIO
        val pGPR      = new GPRIO
        val pITrace   = new ITraceIO
    });

    val mSysDPIDirect      = Module(new SysDPIDirect)
    val mMemDPIDirectComob = Module(new MemDPIDirectComb)

    val mGPR = Module(new GPR())
    val mCSR = Module(new CSR())

    val mIFU = Module(new IFU())
    val mIDU = Module(new IDU())
    val mEXU = Module(new EXU())
    val mWBU = Module(new WBU())

    io.oEndData  := mGPR.io.oRdEndData
    io.oInstCall := Mux((mIDU.io.oInstName === INST_NAME_JAL) ||
                       ((mIDU.io.oInstName === INST_NAME_JALR) &&
                        (mIDU.io.oInstRDAddr =/= 0.U)), true.B, false.B)
    io.oInstRet  := Mux(((mIDU.io.oInstName === INST_NAME_JALR) &&
                         (mIDU.io.oInstRDAddr === 0.U)), true.B, false.B)

    io.pIFU.oPC   := mIFU.io.oPC
    io.pIFU.oInst := mMemDPIDirectComob.io.oMemRdDataInst

    io.pGPR <> mGPR.io.pGPR

    io.pITrace.pCTR.oInstName  := mIDU.io.oInstName
    io.pITrace.pCTR.oStateCurr := STATE_X
    io.pITrace.pCTR.oPCWrEn    := EN_TR
    io.pITrace.pCTR.oPCWrSrc   := DontCare
    io.pITrace.pCTR.oPCNextEn  := DontCare
    io.pITrace.pCTR.oPCJumpEn  := mIDU.io.oJmpEn
    io.pITrace.pCTR.oMemRdEn   := EN_TR
    io.pITrace.pCTR.oMemRdSrc  := DontCare
    io.pITrace.pCTR.oMemWrEn   := mIDU.io.oMemWrEn
    io.pITrace.pCTR.oMemByt    := mIDU.io.oMemByt
    io.pITrace.pCTR.oIRWrEn    := DontCare
    io.pITrace.pCTR.oGPRWrEn   := mIDU.io.oGPRWrEn
    io.pITrace.pCTR.oGPRWrSrc  := mIDU.io.oGPRWrSrc
    io.pITrace.pCTR.oALUType   := mIDU.io.oALUType
    io.pITrace.pCTR.oALURS1    := DontCare
    io.pITrace.pCTR.oALURS2    := DontCare

    io.pITrace.pIDU.oRS1Addr := mIDU.io.oInstRS1Addr
    io.pITrace.pIDU.oRS2Addr := mIDU.io.oInstRS2Addr
    io.pITrace.pIDU.oRDAddr  := mIDU.io.oInstRDAddr
    io.pITrace.pIDU.oRS1Data := mIDU.io.oALURS1Val
    io.pITrace.pIDU.oRS2Data := mIDU.io.oALURS2Val
    io.pITrace.pIDU.oEndData := mGPR.io.oRdEndData
    io.pITrace.pIDU.oImmData := mIDU.io.oInstImmVal

    io.pITrace.pEXU.oPCNext    := DontCare
    io.pITrace.pEXU.oPCJump    := DontCare
    io.pITrace.pEXU.oALUZero   := DontCare
    io.pITrace.pEXU.oALUOut    := mEXU.io.oALUOut
    io.pITrace.pEXU.oMemWrData := mEXU.io.oMemWrData

    io.pITrace.pLSU <> DontCare
    io.pITrace.pWBU <> DontCare

    val rInstName = RegInit(INST_NAME_X)
    rInstName := mIDU.io.oInstName
    mSysDPIDirect.io.iEbreakFlag := Mux(rInstName === INST_NAME_EBREAK, 1.U, 0.U)

    mMemDPIDirectComob.io.iMemRdEn       := true.B
    mMemDPIDirectComob.io.iMemRdAddrInst := mIFU.io.oPC
    mMemDPIDirectComob.io.iMemRdAddrLoad := mEXU.io.oMemRdAddr
    mMemDPIDirectComob.io.iMemWrEn       := mEXU.io.oMemWrEn
    mMemDPIDirectComob.io.iMemWrAddr     := mEXU.io.oMemWrAddr
    mMemDPIDirectComob.io.iMemWrData     := mEXU.io.oMemWrData
    mMemDPIDirectComob.io.iMemWrLen      := mEXU.io.oMemWrLen

    mGPR.io.iRd1Addr := mIDU.io.oInstRS1Addr
    mGPR.io.iRd2Addr := mIDU.io.oInstRS2Addr
    mGPR.io.iWrEn    := mWBU.io.oGPRWrEn
    mGPR.io.iWrAddr  := mWBU.io.oGPRWrAddr
    mGPR.io.iWrData  := mWBU.io.oGPRWrData

    mCSR.io.iCSRWrEn       := mEXU.io.oCSRWrEn
    mCSR.io.iCSRWrMEn      := mEXU.io.oCSRWrMEn
    mCSR.io.iCSRRdAddr     := mIDU.io.oInstCSRAddr
    mCSR.io.iCSRRdMSTAAddr := CSR_MSTATUS
    mCSR.io.iCSRRdMTVEAddr := CSR_MTVEC
    mCSR.io.iCSRRdMEPCAddr := CSR_MEPC
    mCSR.io.iCSRRdMCAUAddr := CSR_MCAUSE
    mCSR.io.iCSRWrAddr     := mEXU.io.oCSRWrAddr
    mCSR.io.iCSRWrMEPCAddr := mEXU.io.oCSRWrMEPCAddr
    mCSR.io.iCSRWrMCAUAddr := mEXU.io.oCSRWrMCAUAddr
    mCSR.io.iCSRWrData     := mEXU.io.oCSRWrData
    mCSR.io.iCSRWrMEPCData := mEXU.io.oCSRWrMEPCData
    mCSR.io.iCSRWrMCAUData := mEXU.io.oCSRWrMCAUData

    mIFU.io.iJmpEn := mEXU.io.oJmpEn
    mIFU.io.iJmpPC := mEXU.io.oJmpPC

    mIDU.io.iInst       := mMemDPIDirectComob.io.oMemRdDataInst
    mIDU.io.iInstRS1Val := mGPR.io.oRd1Data
    mIDU.io.iInstRS2Val := mGPR.io.oRd2Data
    mIDU.io.iInstCSRVal := mCSR.io.oCSRRdData
    mIDU.io.iPC         := mIFU.io.oPC

    mEXU.io.iInstRS1Addr := mIDU.io.oInstRS1Addr
    mEXU.io.iInstRS2Addr := mIDU.io.oInstRS2Addr
    mEXU.io.iInstRDAddr  := mIDU.io.oInstRDAddr
    mEXU.io.iInstCSRAddr := mIDU.io.oInstCSRAddr
    mEXU.io.iInstRS1Val  := mIDU.io.oInstRS1Val
    mEXU.io.iInstRS2Val  := mIDU.io.oInstRS2Val
    mEXU.io.iInstCSRVal  := mIDU.io.oInstCSRVal
    mEXU.io.iPC          := mIFU.io.oPC
    mEXU.io.iMemRdData   := mMemDPIDirectComob.io.oMemRdDataLoad
    mEXU.io.iInstName    := mIDU.io.oInstName
    mEXU.io.iALUType     := mIDU.io.oALUType
    mEXU.io.iALURS1Val   := mIDU.io.oALURS1Val
    mEXU.io.iALURS2Val   := mIDU.io.oALURS2Val
    mEXU.io.iJmpEn       := mIDU.io.oJmpEn
    mEXU.io.iMemWrEn     := mIDU.io.oMemWrEn
    mEXU.io.iMemByt      := mIDU.io.oMemByt
    mEXU.io.iGPRWrEn     := mIDU.io.oGPRWrEn
    mEXU.io.iGPRWrSrc    := mIDU.io.oGPRWrSrc

    mWBU.io.iGPRWrEn   := mEXU.io.oGPRWrEn
    mWBU.io.iGPRWrAddr := mEXU.io.oGPRWrAddr
    mWBU.io.iGPRWrData := mEXU.io.oGPRWrData
}
