package util

import chisel3._
import chisel3.util._

object Base {
    val REGS_NUM   = 32
    val DATA_WIDTH = 64

    val INST_TYPE_WIDTH = 3
    val INST_TYPE_X = 0.U(INST_TYPE_WIDTH.W)
    val INST_TYPE_R = 1.U(INST_TYPE_WIDTH.W)
    val INST_TYPE_I = 2.U(INST_TYPE_WIDTH.W)
    val INST_TYPE_S = 3.U(INST_TYPE_WIDTH.W)
    val INST_TYPE_B = 4.U(INST_TYPE_WIDTH.W)
    val INST_TYPE_U = 5.U(INST_TYPE_WIDTH.W)
    val INST_TYPE_J = 6.U(INST_TYPE_WIDTH.W)

    val ALU_TYPE_WIDTH = 3
    val ALU_TYPE_X = 0.U(ALU_TYPE_WIDTH.W)
    val ALU_TYPE_ADD = 1.U(ALU_TYPE_WIDTH.W)

    val ALU_RS1_WIDTH = 3
    val ALU_RS1_X = 0.U(ALU_RS1_WIDTH)
    val ALU_RS1_R = 1.U(ALU_RS1_WIDTH)
    val ALU_RS1_I = 2.U(ALU_RS1_WIDTH)

    val ALU_RS2_WIDTH = 3
    val ALU_RS2_X = 0.U(ALU_RS2_WIDTH)
    val ALU_RS2_R = 1.U(ALU_RS2_WIDTH)
    val ALU_RS2_IMI = 2.U(ALU_RS2_WIDTH)
    val ALU_RS2_IMS = 3.U(ALU_RS2_WIDTH)

    val MEM_WIDTH = 3
    val MEM_W_X = 0.U(MEM_WIDTH)
    val MEM_W_T = 1.U(MEM_WIDTH)
    val MEM_W_F = 2.U(MEM_WIDTH)

    val REG_WIDTH 3
    val REG_W_X = 0.U(REG_WIDTH)
    val REG_W_T = 0.U(REG_WIDTH)
    val REG_W_F = 0.U(REG_WIDTH)

    val CSR_WIDTH 3
    val CSR_W_X = 0.U(CSR_WIDTH)
    val CSR_W_T = 1.U(CSR_WIDTH)
    val CSR_W_F = 2.U(CSR_WIDTH)
}
