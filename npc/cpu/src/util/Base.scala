package cpu.util

import chisel3._
import chisel3.util._

object Base {
    val REG_NUM      = 32
    val MEM_NUM      = 128

    val REG_WIDTH    = 5
    val BYTE_WIDTH   = 8
    val ADDR_WIDTH   = 32
    val DATA_WIDTH   = 64
    val SIGNAL_WIDTH = 10

    val INST_NAME_X      = 0.U
    val INST_NAME_SLL    = 1.U
    val INST_NAME_SLLI   = 2.U
    val INST_NAME_SRLI   = 3.U
    val INST_NAME_SRA    = 4.U
    val INST_NAME_SRAI   = 5.U
    val INST_NAME_SLLW   = 6.U
    val INST_NAME_SLLIW  = 7.U
    val INST_NAME_SRLW   = 8.U
    val INST_NAME_SRLIW  = 9.U
    val INST_NAME_SRAW   = 10.U
    val INST_NAME_SRAIW  = 11.U
    val INST_NAME_ADD    = 12.U
    val INST_NAME_ADDI   = 13.U
    val INST_NAME_SUB    = 14.U
    val INST_NAME_LUI    = 15.U
    val INST_NAME_AUIPC  = 16.U
    val INST_NAME_ADDW   = 17.U
    val INST_NAME_ADDIW  = 18.U
    val INST_NAME_SUBW   = 19.U
    val INST_NAME_XOR    = 20.U
    val INST_NAME_XORI   = 21.U
    val INST_NAME_OR     = 22.U
    val INST_NAME_ORI    = 23.U
    val INST_NAME_AND    = 24.U
    val INST_NAME_ANDI   = 25.U
    val INST_NAME_SLT    = 26.U
    val INST_NAME_SLTU   = 27.U
    val INST_NAME_SLTIU  = 28.U
    val INST_NAME_BEQ    = 29.U
    val INST_NAME_BNE    = 30.U
    val INST_NAME_BLT    = 31.U
    val INST_NAME_BGE    = 32.U
    val INST_NAME_BLTU   = 33.U
    val INST_NAME_BGEU   = 34.U
    val INST_NAME_JAL    = 35.U
    val INST_NAME_JALR   = 36.U
    val INST_NAME_LB     = 37.U
    val INST_NAME_LH     = 38.U
    val INST_NAME_LBU    = 39.U
    val INST_NAME_LHU    = 40.U
    val INST_NAME_LW     = 41.U
    val INST_NAME_LWU    = 42.U
    val INST_NAME_LD     = 43.U
    val INST_NAME_SB     = 44.U
    val INST_NAME_SH     = 45.U
    val INST_NAME_SW     = 46.U
    val INST_NAME_SD     = 47.U
    val INST_NAME_EBREAK = 48.U
    val INST_NAME_MUL    = 49.U
    val INST_NAME_MULW   = 50.U
    val INST_NAME_DIVU   = 51.U
    val INST_NAME_DIVW   = 52.U
    val INST_NAME_DIVUW  = 53.U
    val INST_NAME_REMU   = 54.U
    val INST_NAME_REMW   = 55.U

    val ALU_TYPE_X     =  0.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_ADD   =  1.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_SUB   =  2.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_AND   =  3.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_OR    =  4.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_XOR   =  5.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_SLT   =  6.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_SLTU  =  7.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_SLL   =  8.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_SLLW  =  9.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_SRL   = 10.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_SRLW  = 11.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_SRLIW = 12.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_SRA   = 13.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_SRAW  = 14.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_SRAIW = 15.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_BEQ   = 16.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_BNE   = 17.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_BLT   = 18.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_BGE   = 19.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_BLTU  = 20.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_BGEU  = 21.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_JALR  = 22.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_MUL   = 23.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_DIVU  = 24.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_DIVW  = 25.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_DIVUW = 26.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_REMU  = 27.U(SIGNAL_WIDTH.W)
    val ALU_TYPE_REMW  = 28.U(SIGNAL_WIDTH.W)

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

    val MEM_BYT_X = 0.U(SIGNAL_WIDTH.W)
    val MEM_BYT_1_U = 1.U(SIGNAL_WIDTH.W)
    val MEM_BYT_2_U = 2.U(SIGNAL_WIDTH.W)
    val MEM_BYT_4_U = 3.U(SIGNAL_WIDTH.W)
    val MEM_BYT_8_U = 4.U(SIGNAL_WIDTH.W)
    val MEM_BYT_1_S = 5.U(SIGNAL_WIDTH.W)
    val MEM_BYT_2_S = 6.U(SIGNAL_WIDTH.W)
    val MEM_BYT_4_S = 7.U(SIGNAL_WIDTH.W)
    val MEM_BYT_8_S = 8.U(SIGNAL_WIDTH.W)

    val REG_WR_T  = 1.U(SIGNAL_WIDTH.W)
    val REG_WR_F  = 0.U(SIGNAL_WIDTH.W)

    val REG_WR_SRC_X   = 0.U(SIGNAL_WIDTH.W)
    val REG_WR_SRC_ALU = 1.U(SIGNAL_WIDTH.W)
    val REG_WR_SRC_MEM = 2.U(SIGNAL_WIDTH.W)
    val REG_WR_SRC_PC  = 3.U(SIGNAL_WIDTH.W)

    val CSR_WR_T  = 1.U(SIGNAL_WIDTH.W)
    val CSR_WR_F  = 0.U(SIGNAL_WIDTH.W)
}
