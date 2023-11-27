package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class IDUIO extends Bundle with ConfigIO {
    val oRS1Addr = Output(UInt(DATA_WIDTH.W))
    val oRS2Addr = Output(UInt(DATA_WIDTH.W))
    val oRDAddr  = Output(UInt(DATA_WIDTH.W))
    val oRS1Data = Output(UInt(DATA_WIDTH.W))
    val oRS2Data = Output(UInt(DATA_WIDTH.W))
    val oEndData = Output(UInt(DATA_WIDTH.W))
    val oImmData = Output(UInt(DATA_WIDTH.W))
}

class IDU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iPC        =  Input(UInt(DATA_WIDTH.W))
        val iInst      =  Input(UInt(INST_WIDTH.W))
        val iGPRWrData =  Input(UInt(DATA_WIDTH.W))

        val bCTRIO     = new CTRIO
        val bGPRIO     = new GPRIO
        val bIDUIO     = new IDUIO
    })

    val mCTR = Module(new CTR)
    mCTR.io.iPC   := io.iPC
    mCTR.io.iInst := io.iInst

    io.bCTRIO <> mCTR.io.bCTRIO

    val wInst = io.iInst

    val mGPR = Module(new GPR)
    val wRS1Addr = wInst(19, 15)
    val wRS2Addr = wInst(24, 20)
    val wRDAddr  = wInst(11, 07)

    mGPR.io.iWrEn      := io.bCTRIO.oGPRWrEn
    mGPR.io.iRd1Addr   := wRS1Addr
    mGPR.io.iRd2Addr   := wRS2Addr
    mGPR.io.iWrAddr    := wRDAddr
    mGPR.io.iWrData    := io.iGPRWrData

    io.bGPRIO          <> mGPR.io.bGPRIO
    io.bIDUIO.oRS1Addr := wRS1Addr
    io.bIDUIO.oRS2Addr := wRS2Addr
    io.bIDUIO.oRDAddr  := wRDAddr

    val mGPRRS1 = Module(new GPRRS1)
    val mGPRRS2 = Module(new GPRRS2)
    mGPRRS1.io.iData := mGPR.io.oRd1Data
    mGPRRS2.io.iData := mGPR.io.oRd2Data

    io.bIDUIO.oRS1Data := mGPRRS1.io.oData
    io.bIDUIO.oRS2Data := mGPRRS2.io.oData
    io.bIDUIO.oEndData := mGPR.io.oRdEndData

    val mImmExten = Module(new ImmExten)
    mImmExten.io.iInst   := wInst
    mImmExten.io.iALURS2 := io.bCTRIO.oALURS2

    io.bIDUIO.oImmData := mImmExten.io.oImmData
}
