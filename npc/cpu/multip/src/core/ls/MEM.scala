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

    val mMem     = SyncReadMem(MEMS_NUM, UInt(DATA_WIDTH.W))
    val mMemCell = mMem(io.iAddr)

    when (io.iWrEn) {
        mMemCell := io.iWrData
    }
    .otherwise {
        io.oRdData := mMemCell
    }
}

class MEMPortDual extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iRdEn   =  Input(Bool())
        val iWrEn   =  Input(Bool())
        val iAddr   =  Input(UInt(DATA_WIDTH.W))
        val iWrData =  Input(UInt(DATA_WIDTH.W))
        val iWrByt  =  Input(UInt(SIGS_WIDTH.W))

        val oRdData = Output(UInt(DATA_WIDTH.W))
    })

    val mMem  = SyncReadMem(MEMS_NUM, UInt(DATA_WIDTH.W))
    // val wAddr = Mux(io.iAddr =/= DATA_ZERO, (io.iAddr - "x80000000".U(DATA_WIDTH.W)).asUInt, DontCare)

    val wAddr = (io.iAddr - 0x80000000.U)

    mMem(0.U) := "0x00500093".U(DATA_WIDTH.W)
    mMem(1.U) := "0x00A08113".U(DATA_WIDTH.W)
    mMem(2.U) := "0x00100073".U(DATA_WIDTH.W)

    val mMemCell = mMem(wAddr)

    when (io.iWrEn) {
        mMemCell := MuxLookup(
            io.iWrByt,
            io.iWrData,
            Seq(
                MEM_BYT_1_U -> Cat(mMemCell(63, 8),  io.iWrData(7,  0)),
                MEM_BYT_2_U -> Cat(mMemCell(63, 16), io.iWrData(15, 0)),
                MEM_BYT_4_U -> Cat(mMemCell(63, 32), io.iWrData(31, 0)),
                MEM_BYT_8_U -> io.iWrData
            )
        )
    }
    .otherwise {
        mMemCell := DontCare
    }

    when (io.iRdEn) {
        io.oRdData := mMemCell
    }
    .otherwise {
        io.oRdData := DontCare
    }
}
