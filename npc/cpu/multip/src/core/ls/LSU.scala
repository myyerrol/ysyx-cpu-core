package cpu.core

import chisel3._
import chisel3.util._

import cpu.blackbox._
import cpu.common._

class LSUIO extends Bundle with ConfigIO {
    val oMemRdEn       = Output(Bool())
    val oMemRdAddrInst = Output(UInt(DATA_WIDTH.W))
    val oMemRdAddrLoad = Output(UInt(DATA_WIDTH.W))
    val oMemWrEn       = Output(Bool())
    val oMemWrAddr     = Output(UInt(DATA_WIDTH.W))
    val oMemWrData     = Output(UInt(DATA_WIDTH.W))
    val oMemWrLen      = Output(UInt(BYTE_WIDTH.W))

    val oMemRdDataInst = Output(UInt(DATA_WIDTH.W))
    val oMemRdDataLoad = Output(UInt(DATA_WIDTH.W))
    val oMemRdData     = Output(UInt(DATA_WIDTH.W))
}

class LSU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iMemRdEn       = Input(Bool())
        val iMemRdSrc      = Input(UInt(SIGS_WIDTH.W))
        val iMemWrEn       = Input(Bool())
        val iMemByt        = Input(UInt(SIGS_WIDTH.W))

        val iPC            = Input(UInt(DATA_WIDTH.W))
        val iALUOut        = Input(UInt(DATA_WIDTH.W))
        val iMemWrData     = Input(UInt(DATA_WIDTH.W))

        val iMemRdDataInst = Input(UInt(INST_WIDTH.W))
        val iMemRdDataLoad = Input(UInt(DATA_WIDTH.W))

        val lsuio          = new LSUIO
    })

    io.lsuio.oMemRdEn       := io.iMemRdEn
    io.lsuio.oMemRdAddrInst := io.iPC
    io.lsuio.oMemRdAddrLoad := io.iALUOut

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
    if (MEMS_INT.equals("DPI")) {
        io.lsuio.oMemRdDataInst := io.iMemRdDataInst
        io.lsuio.oMemRdDataLoad := io.iMemRdDataLoad

        mMRU.io.iData := io.iMemRdDataLoad
    }
    else if (MEMS_INT.equals("MEMI")) {
        val mMEMI = Module(new MEMPortDualI)
        mMEMI.io.iClock := clock
        mMEMI.io.iReset := reset

        mMEMI.io.bMEMPortDualIO.iRdEn := io.iMemRdEn
        mMEMI.io.bMEMPortDualIO.iWrEn := io.iMemWrEn

        val wAddr = Wire(UInt(DATA_WIDTH.W))
        val rAddr = RegInit(DATA_ZERO)

        val wMemRdDataInst = Wire(UInt(DATA_WIDTH.W))
        val rMemRdDataInst = RegInit(DATA_ZERO)

        // val wMemRdDataLoad = Wire(UInt(DATA_WIDTH.W))

        when (io.iMemRdEn) {
            when (io.iMemRdSrc === MEM_RD_SRC_PC) {
                wAddr := io.iPC
                rAddr := io.iPC

                wMemRdDataInst := mMEMI.io.bMEMPortDualIO.oRdData
                rMemRdDataInst := mMEMI.io.bMEMPortDualIO.oRdData
            }
            .elsewhen (io.iMemRdSrc === MEM_RD_SRC_ALU) {
                wAddr := io.iALUOut
                wMemRdDataInst := rMemRdDataInst
            }
            .otherwise {
                wAddr := rAddr
                wMemRdDataInst := rMemRdDataInst
            }
        }
        .otherwise {
            wAddr := rAddr
            wMemRdDataInst := rMemRdDataInst
        }

        // when (io.iMemWrEn) {
        //     wAddr := io.iALUOut
        //     rAddr := io.iALUOut
        //     mMEMI.io.bMEMPortDualIO.iWrData := io.iMemWrData
        //     mMEMI.io.bMEMPortDualIO.iWrByt  := io.iMemByt
        // }
        // .otherwise {
        //     wAddr := rAddr
        //     // mMEMI.io.bMEMPortDualIO.iWrData := DATA_ZERO
        //     // mMEMI.io.bMEMPortDualIO.iWrByt  := MEM_BYT_X
        // }

        mMEMI.io.bMEMPortDualIO.iAddr := wAddr
        io.lsuio.oMemRdDataInst := wMemRdDataInst
        io.lsuio.oMemRdDataLoad := 0.U

        mMRU.io.iData := mMEMI.io.bMEMPortDualIO.oRdData
    }

    io.lsuio.oMemRdData := mMRU.io.oData
}
