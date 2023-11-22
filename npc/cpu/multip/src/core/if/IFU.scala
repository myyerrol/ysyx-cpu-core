package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class IFU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iInstName  =  Input(UInt(SIGS_WIDTH.W))
        val iPCWrEn    =  Input(Bool())
        val iPCWrSrc   =  Input(UInt(SIGS_WIDTH.W))
        val iIRWrEn    =  Input(Bool())

        val iPCNext    =  Input(UInt(DATA_WIDTH.W))
        val iPCJump    =  Input(UInt(DATA_WIDTH.W))
        val iALUZero   =  Input(Bool())
        val iInst      =  Input(UInt(DATA_WIDTH.W))

        val oPC        = Output(UInt(DATA_WIDTH.W))
        val oInst      = Output(UInt(DATA_WIDTH.W))
    })

    val rPC  = RegInit(ADDR_SIM_START)

    val wNPC = WireInit(ADDR_SIM_START)

    when (io.iPCWrSrc === PC_WR_SRC_NEXT) {
        wNPC := io.iPCNext
    }
    .elsewhen (io.iPCWrSrc === PC_WR_SRC_JUMP) {
        when (io.iInstName === INST_NAME_BEQ  ||
              io.iInstName === INST_NAME_BNE  ||
              io.iInstName === INST_NAME_BLT  ||
              io.iInstName === INST_NAME_BGE  ||
              io.iInstName === INST_NAME_BLTU ||
              io.iInstName === INST_NAME_BGEU) {
            wNPC := Mux(io.iALUZero === 1.U, io.iPCJump, io.iPCNext)
        }
        .otherwise {
            wNPC := io.iPCJump
        }
    }

    when (io.iPCWrEn) {
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
