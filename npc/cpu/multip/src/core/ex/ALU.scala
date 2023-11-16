package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class ALU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iALUType =  Input(UInt(SIGS_WIDTH.W))
        val iALURS1  =  Input(UInt(SIGS_WIDTH.W))
        val iALURS2  =  Input(UInt(SIGS_WIDTH.W))
        val iPC      =  Input(UInt(DATA_WIDTH.W))
        val iRS1Data =  Input(UInt(DATA_WIDTH.W))
        val iRS2Data =  Input(UInt(DATA_WIDTH.W))
        val iImmData =  Input(UInt(DATA_WIDTH.W))

        val oALUOut  = Output(UInt(DATA_WIDTH.W))
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
            ALU_RS2_NPC   -> 4.U(DATA_WIDTH.W)
        )
    )

    val wALUOut = MuxLookup(
        io.iALUType,
        DATA_ZERO,
        Seq(
            ALU_TYPE_ADD -> (wRS1Data + wRS2Data)
        )
    )

    val rALUOut = RegNext(wALUOut)

    io.oALUOut := rALUOut
}

