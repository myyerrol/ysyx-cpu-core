package cpu.dpi

import chisel3._;
import chisel3.util._

import cpu.common._

class DPI extends BlackBox with ConfigInst {
    val io = IO(new Bundle {
        // 从处理器核到仿真环境的数据
        val iEbreakFlag    = Input(UInt(BYTE_WIDTH.W))
        val iMemRdAddrInst = Input(UInt(DATA_WIDTH.W))
        val iMemRdAddrLoad = Input(UInt(DATA_WIDTH.W))
        val iMemWrEn       = Input(Bool())
        val iMemWrAddr     = Input(UInt(DATA_WIDTH.W))
        val iMemWrData     = Input(UInt(DATA_WIDTH.W))
        val iMemWrLen      = Input(UInt(BYTE_WIDTH.W))
        val iRegData       = Input(UInt(DATA_WIDTH.W))

        // 从仿真环境到处理器核的数据
        val oMemRdDataInst = Output(UInt(DATA_WIDTH.W))
        val oMemRdDataLoad = Output(UInt(DATA_WIDTH.W))
        val oRegAddr       = Output(UInt(DATA_WIDTH.W))
    })
}
