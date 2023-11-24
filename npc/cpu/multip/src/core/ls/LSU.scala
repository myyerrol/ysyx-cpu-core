package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class LSUIO extends Bundle with ConfigIO {
    // 通过DPI-C将内存指令和数据地址发给仿真环境
    val oMemRdEn       = Output(Bool())
    val oMemRdAddrInst = Output(UInt(DATA_WIDTH.W))
    val oMemRdAddrLoad = Output(UInt(DATA_WIDTH.W))
    val oMemRdDataInst = Output(UInt(DATA_WIDTH.W))
    val oMemRdDataLoad = Output(UInt(DATA_WIDTH.W))
    val oMemWrEn       = Output(Bool())
    val oMemWrAddr     = Output(UInt(DATA_WIDTH.W))
    val oMemWrData     = Output(UInt(DATA_WIDTH.W))
    val oMemWrLen      = Output(UInt(BYTE_WIDTH.W))

    val oMemRdData     = Output(UInt(DATA_WIDTH.W))
}

class LSU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iPC            = Input(UInt(DATA_WIDTH.W))
        val iALUOut        = Input(UInt(DATA_WIDTH.W))
        val iMemRdEn       = Input(Bool())
        val iMemWrEn       = Input(Bool())
        val iMemByt        = Input(UInt(SIGS_WIDTH.W))
        val iMemWrData     = Input(UInt(DATA_WIDTH.W))
        // 通过DPI-C从仿真环境获取到的内存指令和数据
        val iMemRdDataInst = Input(UInt(INST_WIDTH.W))
        val iMemRdDataLoad = Input(UInt(DATA_WIDTH.W))

        val lsuio          = new LSUIO
    })

    io.lsuio.oMemRdEn       := io.iMemRdEn
    io.lsuio.oMemRdAddrInst := io.iPC
    io.lsuio.oMemRdAddrLoad := io.iALUOut
    io.lsuio.oMemRdDataInst := io.iMemRdDataInst
    io.lsuio.oMemRdDataLoad := io.iMemRdDataLoad

    when (io.iMemWrEn) {
        io.lsuio.oMemWrEn   := true.B
        io.lsuio.oMemWrAddr := io.iALUOut
        io.lsuio.oMemWrData := io.iMemWrData
        io.lsuio.oMemWrLen  := MuxLookup(
            io.iMemByt,
            8.U(BYTE_WIDTH.W),
            Seq(
                MEM_BYT_1_U -> 1.U(BYTE_WIDTH.W),
                MEM_BYT_2_U -> 2.U(BYTE_WIDTH.W),
                MEM_BYT_4_U -> 4.U(BYTE_WIDTH.W),
                MEM_BYT_8_U -> 8.U(BYTE_WIDTH.W),
            )
        )
    }
    .otherwise {
        io.lsuio.oMemWrEn   := false.B
        io.lsuio.oMemWrAddr := DATA_ZERO
        io.lsuio.oMemWrData := DATA_ZERO
        io.lsuio.oMemWrLen  := DATA_ZERO
    }

    val mMRU = Module(new MRU)
    mMRU.io.iData := io.iMemRdDataLoad

    io.lsuio.oMemRdData := mMRU.io.oData
}
