package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class MEMPortDualIO extends Bundle with ConfigIO {
    val iRdEn   =  Input(Bool())
    val iWrEn   =  Input(Bool())
    val iAddr   =  Input(UInt(DATA_WIDTH.W))
    val iWrData =  Input(UInt(DATA_WIDTH.W))
    val iWrByt  =  Input(UInt(SIGS_WIDTH.W))

    val oRdData = Output(UInt(DATA_WIDTH.W))
}

class MEMPortDual extends Module with ConfigInst {
    val io = IO(new Bundle {
        val bMEMPortDualIO = new MEMPortDualIO
    })

    val wAddr = (io.bMEMPortDualIO.iAddr - "x80000000".U) / 4.U

    val mMem     = SyncReadMem(MEMS_NUM, UInt(DATA_WIDTH.W))
    val mMemCell = mMem(wAddr)

    val wWrData = io.bMEMPortDualIO.iWrData

    when (io.bMEMPortDualIO.iRdEn) {
        io.bMEMPortDualIO.oRdData := mMemCell
    }
    .otherwise {
        io.bMEMPortDualIO.oRdData := DATA_ZERO
    }

    when (io.bMEMPortDualIO.iWrEn) {
        mMemCell := MuxLookup(
            io.bMEMPortDualIO.iWrByt,
            wWrData,
            Seq(
                MEM_BYT_1_U -> Cat(mMemCell(63, 08), wWrData(07, 0)),
                MEM_BYT_2_U -> Cat(mMemCell(63, 16), wWrData(15, 0)),
                MEM_BYT_4_U -> Cat(mMemCell(63, 32), wWrData(31, 0)),
                MEM_BYT_8_U -> wWrData
            )
        )
    }
}
