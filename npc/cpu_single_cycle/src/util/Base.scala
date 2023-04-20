package cpu.util

import chisel3._
import chisel3.util._

object Base {
    val REG_NUM      = 32
    val MEM_NUM      = 128

    val REG_WIDTH    = 5
    val DATA_WIDTH   = 64
    val SIGNAL_WIDTH = 10

    val ALU_TYPE_X      =  0.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_ADD    =  1.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_JALR   =  9.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_EBREAK = 10.U(SIGNAL_WIDTH.W)

    val ALU_RS1_X     = 0.U(SIGNAL_WIDTH.W)
    val ALU_RS1_R     = 1.U(SIGNAL_WIDTH.W)
    val ALU_RS1_PC    = 2.U(SIGNAL_WIDTH.W)

    val ALU_RS2_X     = 0.U(SIGNAL_WIDTH.W)
    val ALU_RS2_R     = 1.U(SIGNAL_WIDTH.W)
    val ALU_RS2_IMM_I = 2.U(SIGNAL_WIDTH.W)
    val ALU_RS2_IMM_S = 3.U(SIGNAL_WIDTH.W)
    val ALU_RS2_IMM_B = 4.U(SIGNAL_WIDTH.W)
    val ALU_RS2_IMM_U = 5.U(SIGNAL_WIDTH.W)
    val ALU_RS2_IMM_J = 6.U(SIGNAL_WIDTH.W)

    val JMP_T = 1.U(SIGNAL_WIDTH.W)
    val JMP_F = 0.U(SIGNAL_WIDTH.W)

    val MEM_WR_T  = 1.U(SIGNAL_WIDTH.W)
    val MEM_WR_F  = 0.U(SIGNAL_WIDTH.W)

    val REG_WR_T  = 1.U(SIGNAL_WIDTH.W)
    val REG_WR_F  = 0.U(SIGNAL_WIDTH.W)

    val REG_WR_SRC_X   = 0.U(SIGNAL_WIDTH.W)
    val REG_WR_SRC_ALU = 1.U(SIGNAL_WIDTH.W)
    val REG_WR_SRC_MEM = 2.U(SIGNAL_WIDTH.W)
    val REG_WR_SRC_PC  = 3.U(SIGNAL_WIDTH.W)

    val CSR_WR_T  = 1.U(SIGNAL_WIDTH.W)
    val CSR_WR_F  = 0.U(SIGNAL_WIDTH.W)
}
