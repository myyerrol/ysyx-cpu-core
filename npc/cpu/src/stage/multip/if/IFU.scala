package cpu.stage.multip

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.port._

class IFU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iInstName = Input(UInt(SIGS_WIDTH.W))
        val iPCWrEn   = Input(Bool())
        val iPCWrSrc  = Input(UInt(SIGS_WIDTH.W))
        val iIRWrEn   = Input(Bool())

        val iPCNext   = Input(UInt(ADDR_WIDTH.W))
        val iPCJump   = Input(UInt(ADDR_WIDTH.W))
        val iALUZero  = Input(Bool())
        val iInst     = Input(UInt(INST_WIDTH.W))

        val pIFU      = new IFUIO
    })

    val rPC  = RegInit(ADDR_INIT)
    val wNPC = WireInit(ADDR_INIT)

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
        rPC         := wNPC
        io.pIFU.oPC := rPC
    }
    .otherwise {
        io.pIFU.oPC := rPC
    }

    val mIRU = Module(new IRU)
    mIRU.io.iEn   := io.iIRWrEn
    mIRU.io.iData := io.iInst

    io.pIFU.oInst := mIRU.io.oData
}
