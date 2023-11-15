package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class MEM extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iEnWr   =  Input(UInt(DATA_WIDTH.W))
        val iEnRd   =  Input(UInt(DATA_WIDTH.W))
        val iAddr   =  Input(UInt(DATA_WIDTH.W))
        val iDataWr =  Input(UInt(DATA_WIDTH.W))
        val oDataRd = Output(UInt(DATA_WIDTH.W))
    })

    val mem = Mem(4096, UInt(DATA_WIDTH.W))

    when (io.iEnWr) {
        mem(io.iAddr) := io.ioDataWr
    }

    io.iDataRd := Mux(iEnRd, mem(io.addr), DATA_ZERO)
}
