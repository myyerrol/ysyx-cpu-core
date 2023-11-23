package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class MEMPortSingle extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iWrEn   = Input(Bool())
        val iAddr   = Input(UInt(DATA_WIDTH.W))
        val iWrData = Input(UInt(DATA_WIDTH.W))

        val oRdData = Output(UInt(DATA_WIDTH.W))
    })

    val mem = SyncReadMem(MEMS_NUM, UInt(DATA_WIDTH.W))

    when (io.iWrEn) {
        mem(io.iAddr) := io.iWrData
    }
    .otherwise {
        io.oRdData := mem(io.iAddr)
    }
}

class MEMPortDual extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iRdEn   =  Input(Bool())
        val iWrEn   =  Input(Bool())
        val iAddr   =  Input(UInt(DATA_WIDTH.W))
        val iWrData =  Input(UInt(DATA_WIDTH.W))

        val oRdData = Output(UInt(DATA_WIDTH.W))
    })

    val mem = SyncReadMem(MEMS_NUM, UInt(DATA_WIDTH.W))

    when (io.iWrEn) {
        mem(io.iAddr) := io.iWrData
    }

    io.oRdData := Mux(io.iRdEn, mem(io.iAddr), DATA_ZERO)
}

class MEMPortDualReal extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iRdEn   =  Input(Bool())
        val iWrEn   =  Input(Bool())
        val iRdAddr =  Input(UInt(DATA_WIDTH.W))
        val iWrAddr =  Input(UInt(DATA_WIDTH.W))
        val iWrData =  Input(UInt(DATA_WIDTH.W))

        val oRdData = Output(UInt(DATA_WIDTH.W))
    })

    val mem = SyncReadMem(MEMS_NUM, UInt(DATA_WIDTH.W))

     when (io.iWrEn) {
        mem(io.iWrAddr) := io.iWrData
    }

    io.oRdData := Mux(io.iRdEn, mem(io.iRdAddr), DATA_ZERO)
}
