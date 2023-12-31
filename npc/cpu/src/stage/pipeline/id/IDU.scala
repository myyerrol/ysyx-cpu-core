package cpu.stage.pipeline

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.port._

class IDU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iPC        = Input(UInt(ADDR_WIDTH.W))
        val iInst      = Input(UInt(INST_WIDTH.W))
        val iGPRWrData = Input(UInt(DATA_WIDTH.W))

        val pCTR       = new CTRIO
        val pGPR       = new GPRIO
        val pIDU       = new IDUIO
    })

    val mCTR = Module(new CTR)
    mCTR.io.iPC   := io.iPC
    mCTR.io.iInst := io.iInst

    io.pCTR <> mCTR.io.pCTR

    val wInst = io.iInst

    val mGPR = Module(new GPR)
    val wRS1Addr = wInst(19, 15)
    val wRS2Addr = wInst(24, 20)
    val wRDAddr  = wInst(11, 07)

    mGPR.io.iWrEn    := io.pCTR.oGPRWrEn
    mGPR.io.iRd1Addr := wRS1Addr
    mGPR.io.iRd2Addr := wRS2Addr
    mGPR.io.iWrAddr  := wRDAddr
    mGPR.io.iWrData  := io.iGPRWrData

    io.pGPR <> mGPR.io.pGPR

    io.pIDU.oRS1Addr := wRS1Addr
    io.pIDU.oRS2Addr := wRS2Addr
    io.pIDU.oRDAddr  := wRDAddr

    io.pIDU.oRS1Data := mGPR.io.oRd1Data
    io.pIDU.oRS2Data := mGPR.io.oRd2Data
    io.pIDU.oEndData := mGPR.io.oRdEndData

    val mImmExten = Module(new ImmExten)
    mImmExten.io.iInst   := wInst
    mImmExten.io.iALURS2 := io.pCTR.oALURS2

    io.pIDU.oImmData := mImmExten.io.oImmData
}
