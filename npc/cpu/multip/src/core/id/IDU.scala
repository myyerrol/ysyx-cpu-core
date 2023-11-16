package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class IDU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iPC     = Input(UInt(DATA_WIDTH.W))
        val iInst   = Input(UInt(DATA_WIDTH.W))
        val iWrData = Input(UInt(DATA_WIDTH.W))

        val ctrio = new CTRIO

        val oRS1Addr   = Output(UInt(DATA_WIDTH.W))
        val oRS2Addr   = Output(UInt(DATA_WIDTH.W))
        val oRDAddr    = Output(UInt(DATA_WIDTH.W))

        val oRS1Data   = Output(UInt(DATA_WIDTH.W))
        val oRS2Data   = Output(UInt(DATA_WIDTH.W))
        val oRdEndData = Output(UInt(DATA_WIDTH.W))
        val oImmData   = Output(UInt(DATA_WIDTH.W))
    })

    val mCTR = Module(new CTR())
    mCTR.io.iPC   := io.iPC
    mCTR.io.iInst := io.iInst
    io.ctrio      <> mCTR.io.ctrio

    val wInst = io.iInst

    val mGPR = Module(new GPR())
    mGPR.io.iWrEn    := io.ctrio.oGPRWrEn

    val wRS1Addr = wInst(19, 15)
    val wRS2Addr = wInst(24, 20)
    val wRDAddr  = wInst(11, 07)

    io.oRS1Addr  := wRS1Addr
    io.oRS2Addr  := wRS2Addr
    io.oRDAddr   := wRDAddr

    mGPR.io.iRS1Addr := wRS1Addr
    mGPR.io.iRS2Addr := wRS2Addr
    mGPR.io.iRDAddr  := wRDAddr
    mGPR.io.iWrData  := io.iWrData

    val rRS1Data = RegNext(mGPR.io.oRS1Data, DATA_ZERO)
    val rRS2Data = RegNext(mGPR.io.oRS2Data, DATA_ZERO)
    io.oRS1Data   := rRS1Data
    io.oRS2Data   := rRS2Data
    io.oRdEndData := mGPR.io.oRdEndData

    val wImmI     = wInst(31, 20)
    val wImmISext = Cat(Fill(52, wImmI(11)), wImmI)
    val wImmS     = Cat(wInst(31, 25), wInst(11, 7))
    val wImmSSext = Cat(Fill(52, wImmS(11)), wImmS)
    val wImmB     = Cat(wInst(31), wInst(7), wInst(30, 25), wInst(11, 8))
    val wImmBSext = Cat(Fill(51, wImmB(11)), wImmB, 0.U(1.U))
    val wImmU     = wInst(31, 12)
    val wImmUSext = Cat(Fill(32, wImmU(19)), wImmU, Fill(12, 0.U))
    val wImmJ     = Cat(wInst(31), wInst(19, 12), wInst(20), wInst(30, 21))
    val wImmJSext = Cat(Fill(43, wImmJ(19)), wImmJ, 0.U(1.U))

    io.oImmData := MuxLookup(
        io.ctrio.oALURS2,
        DATA_ZERO,
        Seq(
            ALU_RS2_IMM_I -> wImmISext,
            ALU_RS2_IMM_S -> wImmSSext,
            ALU_RS2_IMM_B -> wImmBSext,
            ALU_RS2_IMM_U -> wImmUSext,
            ALU_RS2_IMM_J -> wImmJSext
        )
    )
}
