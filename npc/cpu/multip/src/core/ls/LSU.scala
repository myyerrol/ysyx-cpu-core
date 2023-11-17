package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class LSU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iPC            =  Input(UInt(DATA_WIDTH.W))
        val iALUOut        =  Input(UInt(DATA_WIDTH.W))
        val iMemWrEn       =  Input(Bool())
        val iMemByt        =  Input(UInt(SIGS_WIDTH.W))
        val iMemWrData     =  Input(UInt(DATA_WIDTH.W))

        // 通过DPI-C从仿真环境获取到的内存指令和数据
        val iMemRdDataInst =  Input(UInt(DATA_WIDTH.W))
        val iMemRdDataLoad =  Input(UInt(DATA_WIDTH.W))

        // 通过DPI-C将内存指令和数据地址发给仿真环境
        val oMemRdAddrInst = Output(UInt(DATA_WIDTH.W))
        val oMemRdAddrLoad = Output(UInt(DATA_WIDTH.W))
        val oMemWrEn       = Output(Bool())
        val oMemWrAddr     = Output(UInt(DATA_WIDTH.W))
        val oMemWrData     = Output(UInt(DATA_WIDTH.W))
        val oMemWrLen      = Output(UInt(BYTE_WIDTH.W))

        val oMemRdData     = Output(UInt(DATA_WIDTH.W))
    })

    io.oMemRdAddrInst := io.iPC
    io.oMemRdAddrLoad := io.iALUOut

    when (io.iMemWrEn) {
        io.oMemWrEn   := true.B
        io.oMemWrAddr := io.iALUOut
        io.oMemWrData := io.iMemWrData
        io.oMemWrLen  := MuxLookup(
            io.iMemByt,
            8.U(BYTE_WIDTH.W),
            Seq(
                MEM_BYT_1_U -> 1.U(BYTE_WIDTH.W),
                MEM_BYT_2_U -> 2.U(BYTE_WIDTH.W),
                MEM_BYT_4_U -> 4.U(BYTE_WIDTH.W),
                MEM_BYT_8_U -> 8.U(BYTE_WIDTH.W),
            )
        )
    }.otherwise {
        io.oMemWrEn   := false.B
        io.oMemWrAddr := 0.U(DATA_WIDTH.W)
        io.oMemWrData := 0.U(DATA_WIDTH.W)
        io.oMemWrLen  := 0.U(BYTE_WIDTH.W)
    }

    val mMRU = Module(new MRU())
    mMRU.io.iData := io.iMemRdDataLoad

    io.oMemRdData := mMRU.io.oData
}
