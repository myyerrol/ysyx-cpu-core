package cpu.comp

import chisel3._
import chisel3.util._

import cpu.util.Base._

class MemM extends Module {
    val io = IO(new Bundle {
        val iMemWrEn   = Input(Bool())
        val iMemRdAddr = Input(UInt(DATA_WIDTH.W))
        val iMemWrAddr = Input(UInt(DATA_WIDTH.W))
        val iMemWrData = Input(UInt(DATA_WIDTH.W))

        val oMemRdData = Output(UInt(DATA_WIDTH.W))
    })

    val mem = Mem(MEM_NUM, UInt(DATA_WIDTH.W))

    when (io.iMemWrEn) {
        mem(io.iMemWrAddr) := io.iMemWrData
    }

    io.oMemRdData := mem(io.iMemRdAddr)
}
