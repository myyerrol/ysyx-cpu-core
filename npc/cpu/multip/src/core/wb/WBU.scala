package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class WBU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iMemByt    =  Input(UInt(SIGS_WIDTH.W))
        val iGPRWrSrc  =  Input(UInt(SIGS_WIDTH.W))
        val iALUOut    =  Input(UInt(DATA_WIDTH.W))
        val iMemData   =  Input(UInt(DATA_WIDTH.W))

        val oGPRWrData = Output(UInt(DATA_WIDTH.W))
    })

    when (io.iGPRWrSrc === GPR_WR_SRC_MEM) {
        val wMemData     = io.iMemData
        val wMemDataByt1 = wMemData(07, 0)
        val wMemDataByt2 = wMemData(15, 0)
        val wMemDataByt4 = wMemData(31, 0)
        val wMemDataByt8 = wMemData(63, 0)
        val wMemDataMux = MuxLookup(
            io.iMemByt,
            DATA_ZERO,
            Seq(
                MEM_BYT_1_S -> Cat(Fill(56, wMemDataByt1(7)),  wMemDataByt1),
                MEM_BYT_2_S -> Cat(Fill(48, wMemDataByt2(15)), wMemDataByt2),
                MEM_BYT_1_U -> Cat(Fill(56, 0.U), wMemDataByt1),
                MEM_BYT_2_U -> Cat(Fill(48, 0.U), wMemDataByt2),
                MEM_BYT_4_S -> Cat(Fill(32, wMemDataByt4(31)), wMemDataByt4),
                MEM_BYT_4_U -> Cat(Fill(32, 0.U), wMemDataByt4),
                MEM_BYT_8_S -> wMemDataByt8
            )
        )
        io.oGPRWrData := wMemDataMux
    }
    .elsewhen (io.iGPRWrSrc === GPR_WR_SRC_ALU) {
        io.oGPRWrData := io.iALUOut
    }
    .otherwise {
        io.oGPRWrData := DATA_ZERO
    }

    // io.oGPRWrData := MuxLookup(
    //     io.iGPRWrSrc,
    //     DATA_ZERO,
    //     Seq(
    //         GPR_WR_SRC_ALU -> io.iALUOut,
    //         GPR_WR_SRC_MEM -> wMemDataByt
    //     )
    // )
}
