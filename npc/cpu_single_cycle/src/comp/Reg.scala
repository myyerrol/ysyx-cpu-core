package cpu.comp

import chisel3._
import chisel3.util._

import cpu.util.Base._

class RegM extends Module {
    val io = IO(new Bundle {
        val iRegWrEn    =  Input(Bool())
        val iRegRd1Addr =  Input(UInt(DATA_WIDTH.W))
        val iRegRd2Addr =  Input(UInt(DATA_WIDTH.W))
        val iRegWrAddr  =  Input(UInt(DATA_WIDTH.W))
        val iRegWrData  =  Input(UInt(DATA_WIDTH.W))
        val oRegRd1Data = Output(UInt(DATA_WIDTH.W))
        val oRegRd2Data = Output(UInt(DATA_WIDTH.W))
    })

    val regFile = Mem(REG_NUM, UInt(DATA_WIDTH.W))

    when (io.iRegWrEn) {
        regFile(io.iRegWrAddr) := io.iRegWrData
    }

    io.oRegRd1Data := regFile(io.iRegRd1Addr)
    io.oRegRd2Data := regFile(io.iRegRd2Addr)
}
