package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class WBU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iInstName  =  Input(UInt(SIGS_WIDTH.W))
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
        val wMemDataMux  = MuxLookup(
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
        when (io.iInstName === INST_NAME_ADDW  ||
              io.iInstName === INST_NAME_ADDIW ||
              io.iInstName === INST_NAME_SUBW  ||
              io.iInstName === INST_NAME_SLLW  ||
              io.iInstName === INST_NAME_SLLIW ||
              io.iInstName === INST_NAME_SRLW  ||
              io.iInstName === INST_NAME_SRLIW ||
              io.iInstName === INST_NAME_SRAW  ||
              io.iInstName === INST_NAME_SRAIW ||
              io.iInstName === INST_NAME_MULW  ||
              io.iInstName === INST_NAME_DIVW  ||
              io.iInstName === INST_NAME_DIVUW ||
              io.iInstName === INST_NAME_REMW) {
            val wALUOutByt4 = io.iALUOut(31, 0)
            io.oGPRWrData := Cat(Fill(32, wALUOutByt4(31)), wALUOutByt4)
        }
        .otherwise {
            io.oGPRWrData := io.iALUOut
        }
    }
    .otherwise {
        io.oGPRWrData := DATA_ZERO
    }
}
