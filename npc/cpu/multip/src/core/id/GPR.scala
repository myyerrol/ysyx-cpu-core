package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class GPR extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iWrEn    = Input(UInt(SIGS_WIDTH.W))
        val iRS1Addr = Input(UInt(DATA_WIDTH.W))
        val iRS2Addr = Input(UInt(DATA_WIDTH.W))
        val iRDAddr  = Input(UInt(DATA_WIDTH.W))
        val iWrData  = Input(UInt(DATA_WIDTH.W))

        val oRS1Data = Output(UInt(DATA_WIDTH.W))
        val oRS2Data = Output(UInt(DATA_WIDTH.W))
    })

    val mGPR = Mem(GPR_NUM, UInt(DATA_WIDTH.W))

    when (io.iWrEn) {
        mGPR(io.iRDAddr) := io.iWrData
    }

    io.oRS1Data := mGPR(io.iRS1Addr)
    io.oRS2Data := mGPR(io.iRS2Addr)
}

