package cpu.stage.pipeline

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.module._
import cpu.port._

class EXU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iALUType  = Input(UInt(SIGS_WIDTH.W))
        val iALURS1   = Input(UInt(SIGS_WIDTH.W))
        val iALURS2   = Input(UInt(SIGS_WIDTH.W))

        val iPC       = Input(UInt(ADDR_WIDTH.W))
        val iRS1Data  = Input(UInt(DATA_WIDTH.W))
        val iRS2Data  = Input(UInt(DATA_WIDTH.W))
        val iImmData  = Input(UInt(DATA_WIDTH.W))

        val pEXU      = new EXUIO
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
            ALU_RS2_IMM_J -> io.iImmData
        )
    )

    val mALU = Module(new ALU)
    mALU.io.iType    := io.iALUType
    mALU.io.iRS1Data := wRS1Data
    mALU.io.iRS2Data := wRS2Data

    io.pEXU.oPCNext    := DontCare
    io.pEXU.oPCJump    := DontCare
    io.pEXU.oALUZero   := mALU.io.oZero
    io.pEXU.oALUOut    := mALU.io.oOut
    io.pEXU.oMemWrData := wRS2Data
}
