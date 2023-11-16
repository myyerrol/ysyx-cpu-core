package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class MEM extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iWrEn   =  Input(Bool())
        val iRdEn   =  Input(Bool())
        val iAddr   =  Input(UInt(DATA_WIDTH.W))
        val iWrData =  Input(UInt(DATA_WIDTH.W))

        val oRdData = Output(UInt(DATA_WIDTH.W))
    })

    val mMem = Mem(MEMS_NUM, UInt(DATA_WIDTH.W))

    when (io.iWrEn) {
        mMem(io.iAddr) := io.iWrData
    }

    io.oRdData := Mux(io.iRdEn, mMem(io.iAddr), DATA_ZERO)
}
