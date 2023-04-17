package cpu.util

import chisel3._
import chisel3.util._

object Base {
    val REG_NUM    = 32
    val MEM_NUM    = 128
    val DATA_WIDTH = 64

    val ALU_TYPE_WIDTH = 5
    val ALU_TYPE_X = 0.U(ALU_TYPE_WIDTH.W)
    val ALU_TYPE_ADD = 1.U(ALU_TYPE_WIDTH.W)

    val ALU_RS1_WIDTH = 5
    val ALU_RS1_X = 0.U(ALU_RS1_WIDTH.W)
    val ALU_RS1_R = 1.U(ALU_RS1_WIDTH.W)
    val ALU_RS1_I = 2.U(ALU_RS1_WIDTH.W)

    val ALU_RS2_WIDTH = 5
    val ALU_RS2_X = 0.U(ALU_RS2_WIDTH.W)
    val ALU_RS2_R = 1.U(ALU_RS2_WIDTH.W)
    val ALU_RS2_IMM_I = 2.U(ALU_RS2_WIDTH.W)
    val ALU_RS2_IMM_S = 3.U(ALU_RS2_WIDTH.W)

    val MEM_WIDTH = 5
    val MEM_WR_T = 1.U(MEM_WIDTH.W)
    val MEM_WR_F = 0.U(MEM_WIDTH.W)

    val REG_WIDTH = 5
    val REG_WR_T = 1.U(REG_WIDTH.W)
    val REG_WR_F = 0.U(REG_WIDTH.W)

    val CSR_WIDTH = 5
    val CSR_WR_T = 1.U(CSR_WIDTH.W)
    val CSR_WR_F = 0.U(CSR_WIDTH.W)
}
