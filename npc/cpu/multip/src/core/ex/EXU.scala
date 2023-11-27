package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class EXUIO extends Bundle with ConfigIO {
    val oPCNext    = Output(UInt(DATA_WIDTH.W))
    val oPCJump    = Output(UInt(DATA_WIDTH.W))
    val oALUZero   = Output(Bool())
    val oALUOut    = Output(UInt(DATA_WIDTH.W))
    val oMemWrData = Output(UInt(DATA_WIDTH.W))
}

class EXU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iPCNextEn  =  Input(Bool())
        val iPCJumpEn  =  Input(Bool())
        val iALUType   =  Input(UInt(SIGS_WIDTH.W))
        val iALURS1    =  Input(UInt(SIGS_WIDTH.W))
        val iALURS2    =  Input(UInt(SIGS_WIDTH.W))

        val iPC        =  Input(UInt(DATA_WIDTH.W))
        val iRS1Data   =  Input(UInt(DATA_WIDTH.W))
        val iRS2Data   =  Input(UInt(DATA_WIDTH.W))
        val iImmData   =  Input(UInt(DATA_WIDTH.W))

        val bEXUIO     = new EXUIO
    })

    val wRS1Data = MuxLookup(
        io.iALURS1,
        DATA_ZERO,
        Seq(
            ALU_RS1_PC  -> io.iPC,
            ALU_RS1_GPR -> io.iRS1Data
        )
    )

    val wRS2Data = MuxLookup(
        io.iALURS2,
        DATA_ZERO,
        Seq(
            ALU_RS2_GPR   -> io.iRS2Data,
            ALU_RS2_IMM_I -> io.iImmData,
            ALU_RS2_IMM_S -> io.iImmData,
            ALU_RS2_IMM_B -> io.iImmData,
            ALU_RS2_IMM_U -> io.iImmData,
            ALU_RS2_IMM_J -> io.iImmData,
            ALU_RS2_4     -> 4.U(DATA_WIDTH.W)
        )
    )

    val mALU = Module(new ALU)
    mALU.io.iType    := io.iALUType
    mALU.io.iRS1Data := wRS1Data
    mALU.io.iRS2Data := wRS2Data

    val rPCNext = RegEnable(mALU.io.oOut, DATA_ZERO, io.iPCNextEn)
    val rPCJump = RegEnable(mALU.io.oOut, DATA_ZERO, io.iPCJumpEn)

    val mALUOut = Module(new ALUOut)
    mALUOut.io.iData := mALU.io.oOut

    io.bEXUIO.oPCNext    := rPCNext
    io.bEXUIO.oPCJump    := rPCJump
    io.bEXUIO.oALUZero   := mALU.io.oZero
    io.bEXUIO.oALUOut    := mALUOut.io.oData
    io.bEXUIO.oMemWrData := wRS2Data
}
