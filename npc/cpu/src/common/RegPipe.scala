package cpu.common

import chisel3._
import chisel3.util._

class RegPipe extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iEn    = Input (Bool())

        val iValid = Input (Bool())
        val iReady = Input (Bool())
        val iPC    = Input (UInt(ADDR_WIDTH.W))
        val iInst  = Input (UInt(INST_WIDTH.W))
        val iDataA = Input (UInt(DATA_WIDTH.W))
        val iDataB = Input (UInt(DATA_WIDTH.W))
        val iDataC = Input (UInt(DATA_WIDTH.W))

        val oValid = Output(Bool())
        val oReady = Output(Bool())
        val oPC    = Output(UInt(ADDR_WIDTH.W))
        val oInst  = Output(UInt(INST_WIDTH.W))
        val oDataA = Output(UInt(DATA_WIDTH.W))
        val oDataB = Output(UInt(DATA_WIDTH.W))
        val oDataC = Output(UInt(DATA_WIDTH.W))
    })

    val rValid = RegEnable(io.iValid, false.B,   io.iEn)
    val rReady = RegEnable(io.iReady, false.B,   io.iEn)
    val rPC    = RegEnable(io.iPC,    ADDR_ZERO, io.iEn)
    val rInst  = RegEnable(io.iInst,  INST_ZERO, io.iEn)
    val rDataA = RegEnable(io.iDataA, false.B,   io.iEn)
    val rDataB = RegEnable(io.iDataB, ADDR_ZERO, io.iEn)
    val rDataC = RegEnable(io.iDataC, INST_ZERO, io.iEn)

    io.oValid := rValid
    io.oReady := rReady
    io.oPC    := rPC
    io.oInst  := rInst
    io.oDataA := rDataA
    io.oDataB := rDataB
    io.oDataC := rDataC
}
