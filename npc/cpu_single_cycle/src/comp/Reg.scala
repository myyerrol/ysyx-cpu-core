package cpu.comp

import chisel3._
import chisel3.util._

import cpu.util.Base._

class Reg extends Module {
    val io = IO(new Bundle {
        val iRegWrEn   =  Input(Bool())
        val iRegRdAddr =  Input(UInt(DATA_WIDTH.W))
        val iRegWrAddr =  Input(UInt(DATA_WIDTH.W))
        val iRegWrData =  Input(UInt(DATA_WIDTH.W))
        val oRegRdData = Output(UInt(DATA_WIDTH.W))
    })

    val regFile = Mem(REG_NUM, UInt(DATA_WIDTH.W))

    when (io.iRegWrEn) {
        regFile(io.iRegWrAddr) := io.iRegWrData
    }

    io.oRegRdData := regFile(io.iRegRdAddr)
}
