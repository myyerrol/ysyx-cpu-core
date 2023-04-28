package cpu.dpi

import chisel3._;
import chisel3.util._

import cpu.util.Base._

class DPI extends BlackBox {
    val io = IO(new Bundle {
        val iEbreakFlag    =  Input(UInt(ADDR_WIDTH.W))
        val iMemRdAddrInst =  Input(UInt(DATA_WIDTH.W))
        val iMemRdAddrLoad =  Input(UInt(DATA_WIDTH.W))
        val iMemWrEn       =  Input(Bool())
        val iMemWrAddr     =  Input(UInt(DATA_WIDTH.W))
        val iMemWrData     =  Input(UInt(DATA_WIDTH.W))
        val iMemWrLen      =  Input(UInt(BYTE_WIDTH.W))
        val oMemRdDataInst = Output(UInt(DATA_WIDTH.W))
        val oMemRdDataLoad = Output(UInt(DATA_WIDTH.W))
        // val iRegVal     =  Input(UInt(DATA_WIDTH.W))
        // val oRegAddr    = Output(UInt(DATA_WIDTH.W))
    })
}
