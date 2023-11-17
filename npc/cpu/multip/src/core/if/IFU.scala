package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class IFU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iPCWrEn    =  Input(Bool())
        val iPCWrConEn =  Input(Bool())
        val iPCWrSrc   =  Input(UInt(SIGS_WIDTH.W))
        val iIRWrEn    =  Input(Bool())
        val iNPC       =  Input(UInt(DATA_WIDTH.W))
        val iALUZero   =  Input(Bool())
        val iALUOut    =  Input(UInt(DATA_WIDTH.W))
        val iInst      =  Input(UInt(DATA_WIDTH.W))

        val oPC        = Output(UInt(DATA_WIDTH.W))
        val oInst      = Output(UInt(DATA_WIDTH.W))
    })

    val rPC  = RegInit(ADDR_SIM_START)
    val wNPC = MuxLookup(
        io.iPCWrSrc,
        ADDR_SIM_START,
        Seq(
            PC_WR_SRC_NPC -> io.iNPC,
            PC_WR_SRC_ALU -> io.iALUOut
        )
    )

    val wPCWr = io.iPCWrEn || (io.iPCWrConEn && io.iALUZero)

    when (wPCWr) {
        rPC    := wNPC
        io.oPC := rPC
    }
    .otherwise {
        io.oPC := rPC
    }

    val mIRU = Module(new IRU())
    mIRU.io.iWrEn := io.iIRWrEn
    mIRU.io.iData := io.iInst

    io.oInst := mIRU.io.oData
}
