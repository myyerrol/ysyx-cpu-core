package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class MEM extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iWrEn   =  Input(UInt(SIGS_WIDTH.W))
        val iRdEn   =  Input(UInt(SIGS_WIDTH.W))
        val iAddr   =  Input(UInt(DATA_WIDTH.W))
        val iWrData =  Input(UInt(DATA_WIDTH.W))

        val oRdData = Output(UInt(DATA_WIDTH.W))
    })

    val mMem = Mem(MEM_NUM, UInt(DATA_WIDTH.W))

    when (io.iWrEn === SIG_TRUE) {
        mMem(io.iAddr) := io.iWrData
    }

    io.oRdData := Mux(io.iRdEn === SIG_TRUE, mMem(io.iAddr), DATA_ZERO)
}
