package cpu.common

import chisel3._
import chisel3.util._

class ImmExten extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iInst    = Input (UInt(INST_WIDTH.W))
        val iALURS2  = Input (UInt(SIGS_WIDTH.W))

        val oImmData = Output(UInt(DATA_WIDTH.W))
    })

    val wInst = io.iInst

    val wImmI      = wInst(31, 20)
    val wImmIExten = SignExten(wImmI, DATA_WIDTH)
    val wImmS      = Cat(wInst(31, 25), wInst(11, 7))
    val wImmSExten = SignExten(wImmS, DATA_WIDTH)
    val wImmB      = Cat(wInst(31), wInst(7), wInst(30, 25), wInst(11, 8), 0.U(1.W))
    val wImmBExten = SignExten(wImmB, DATA_WIDTH)
    val wImmU      = Cat(wInst(31, 12), 0.U(12.W))
    val wImmUExten = SignExten(wImmU, DATA_WIDTH)
    val wImmJ      = Cat(wInst(31), wInst(19, 12), wInst(20), wInst(30, 21), 0.U(1.W))
    val wImmJExten = SignExten(wImmJ, DATA_WIDTH)

    io.oImmData := MuxLookup(
        io.iALURS2,
        DATA_ZERO,
        Seq(
            ALU_RS2_IMM_I -> wImmIExten,
            ALU_RS2_IMM_S -> wImmSExten,
            ALU_RS2_IMM_B -> wImmBExten,
            ALU_RS2_IMM_U -> wImmUExten,
            ALU_RS2_IMM_J -> wImmJExten
        )
    )
}
