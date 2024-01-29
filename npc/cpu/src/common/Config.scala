package cpu.common

import chisel3._
import chisel3.util._

trait ConfigIO {
    val RESP_WIDTH = 2
    val MODE_WIDTH = 2
    val GPRS_WIDTH = 5
    val BYTE_WIDTH = 8
    val SIGS_WIDTH = 10
    val INST_WIDTH = 32
    val ADDR_WIDTH = 64
    val DATA_WIDTH = 64
    val MASK_WIDTH = DATA_WIDTH / BYTE_WIDTH

    val GPRS_NUM = 1 << GPRS_WIDTH
    val CSRS_NUM = 4096
    val MEMS_TYP = "DPIDirect" // DPIDirect, DPIAXI4Lite, Embed
}

trait ConfigInst extends ConfigIO {
    val ADDR_INIT = "x80000000".U(ADDR_WIDTH.W)

    val INST_ZERO = "x00000000".U(INST_WIDTH.W)
    val ADDR_ZERO = "x00000000".U(ADDR_WIDTH.W)
    val DATA_ZERO = "x00000000".U(DATA_WIDTH.W)
    val GPRS_10   = 10.U(GPRS_WIDTH.W)

    val EN_TR = true.B
    val EN_FL = false.B

    val STATE_X  = 0.U(SIGS_WIDTH.W)
    val STATE_IF = 1.U(SIGS_WIDTH.W)
    val STATE_ID = 2.U(SIGS_WIDTH.W)
    val STATE_EX = 3.U(SIGS_WIDTH.W)
    val STATE_LS = 4.U(SIGS_WIDTH.W)
    val STATE_WB = 5.U(SIGS_WIDTH.W)

    val INST_NAME_X      =  0.U(SIGS_WIDTH.W)
    val INST_NAME_SLL    =  1.U(SIGS_WIDTH.W)
    val INST_NAME_SLLI   =  2.U(SIGS_WIDTH.W)
    val INST_NAME_SRLI   =  3.U(SIGS_WIDTH.W)
    val INST_NAME_SRA    =  4.U(SIGS_WIDTH.W)
    val INST_NAME_SRAI   =  5.U(SIGS_WIDTH.W)
    val INST_NAME_SLLW   =  6.U(SIGS_WIDTH.W)
    val INST_NAME_SLLIW  =  7.U(SIGS_WIDTH.W)
    val INST_NAME_SRLW   =  8.U(SIGS_WIDTH.W)
    val INST_NAME_SRLIW  =  9.U(SIGS_WIDTH.W)
    val INST_NAME_SRAW   = 10.U(SIGS_WIDTH.W)
    val INST_NAME_SRAIW  = 11.U(SIGS_WIDTH.W)
    val INST_NAME_ADD    = 12.U(SIGS_WIDTH.W)
    val INST_NAME_ADDI   = 13.U(SIGS_WIDTH.W)
    val INST_NAME_SUB    = 14.U(SIGS_WIDTH.W)
    val INST_NAME_LUI    = 15.U(SIGS_WIDTH.W)
    val INST_NAME_AUIPC  = 16.U(SIGS_WIDTH.W)
    val INST_NAME_ADDW   = 17.U(SIGS_WIDTH.W)
    val INST_NAME_ADDIW  = 18.U(SIGS_WIDTH.W)
    val INST_NAME_SUBW   = 19.U(SIGS_WIDTH.W)
    val INST_NAME_XOR    = 20.U(SIGS_WIDTH.W)
    val INST_NAME_XORI   = 21.U(SIGS_WIDTH.W)
    val INST_NAME_OR     = 22.U(SIGS_WIDTH.W)
    val INST_NAME_ORI    = 23.U(SIGS_WIDTH.W)
    val INST_NAME_AND    = 24.U(SIGS_WIDTH.W)
    val INST_NAME_ANDI   = 25.U(SIGS_WIDTH.W)
    val INST_NAME_SLT    = 26.U(SIGS_WIDTH.W)
    val INST_NAME_SLTU   = 27.U(SIGS_WIDTH.W)
    val INST_NAME_SLTIU  = 28.U(SIGS_WIDTH.W)
    val INST_NAME_BEQ    = 29.U(SIGS_WIDTH.W)
    val INST_NAME_BNE    = 30.U(SIGS_WIDTH.W)
    val INST_NAME_BLT    = 31.U(SIGS_WIDTH.W)
    val INST_NAME_BGE    = 32.U(SIGS_WIDTH.W)
    val INST_NAME_BLTU   = 33.U(SIGS_WIDTH.W)
    val INST_NAME_BGEU   = 34.U(SIGS_WIDTH.W)
    val INST_NAME_JAL    = 35.U(SIGS_WIDTH.W)
    val INST_NAME_JALR   = 36.U(SIGS_WIDTH.W)
    val INST_NAME_LB     = 37.U(SIGS_WIDTH.W)
    val INST_NAME_LH     = 38.U(SIGS_WIDTH.W)
    val INST_NAME_LBU    = 39.U(SIGS_WIDTH.W)
    val INST_NAME_LHU    = 40.U(SIGS_WIDTH.W)
    val INST_NAME_LW     = 41.U(SIGS_WIDTH.W)
    val INST_NAME_LWU    = 42.U(SIGS_WIDTH.W)
    val INST_NAME_LD     = 43.U(SIGS_WIDTH.W)
    val INST_NAME_SB     = 44.U(SIGS_WIDTH.W)
    val INST_NAME_SH     = 45.U(SIGS_WIDTH.W)
    val INST_NAME_SW     = 46.U(SIGS_WIDTH.W)
    val INST_NAME_SD     = 47.U(SIGS_WIDTH.W)
    val INST_NAME_ECALL  = 48.U(SIGS_WIDTH.W)
    val INST_NAME_EBREAK = 49.U(SIGS_WIDTH.W)
    val INST_NAME_CSRRW  = 50.U(SIGS_WIDTH.W)
    val INST_NAME_CSRRS  = 51.U(SIGS_WIDTH.W)
    val INST_NAME_MRET   = 52.U(SIGS_WIDTH.W)
    val INST_NAME_MUL    = 53.U(SIGS_WIDTH.W)
    val INST_NAME_MULW   = 54.U(SIGS_WIDTH.W)
    val INST_NAME_DIVU   = 55.U(SIGS_WIDTH.W)
    val INST_NAME_DIVW   = 56.U(SIGS_WIDTH.W)
    val INST_NAME_DIVUW  = 57.U(SIGS_WIDTH.W)
    val INST_NAME_REMU   = 58.U(SIGS_WIDTH.W)
    val INST_NAME_REMW   = 59.U(SIGS_WIDTH.W)

    val PC_WR_SRC_X    = 0.U(SIGS_WIDTH.W)
    val PC_WR_SRC_NEXT = 1.U(SIGS_WIDTH.W)
    val PC_WR_SRC_JUMP = 2.U(SIGS_WIDTH.W)

    val MEM_RD_SRC_X   = 0.U(SIGS_WIDTH.W)
    val MEM_RD_SRC_PC  = 1.U(SIGS_WIDTH.W)
    val MEM_RD_SRC_ALU = 2.U(SIGS_WIDTH.W)

    val MEM_BYT_X   = 0.U(SIGS_WIDTH.W)
    val MEM_BYT_1_U = 1.U(SIGS_WIDTH.W)
    val MEM_BYT_2_U = 2.U(SIGS_WIDTH.W)
    val MEM_BYT_4_U = 3.U(SIGS_WIDTH.W)
    val MEM_BYT_8_U = 4.U(SIGS_WIDTH.W)
    val MEM_BYT_1_S = 5.U(SIGS_WIDTH.W)
    val MEM_BYT_2_S = 6.U(SIGS_WIDTH.W)
    val MEM_BYT_4_S = 7.U(SIGS_WIDTH.W)
    val MEM_BYT_8_S = 8.U(SIGS_WIDTH.W)

    val GPR_WR_SRC_X   = 0.U(SIGS_WIDTH.W)
    val GPR_WR_SRC_ALU = 1.U(SIGS_WIDTH.W)
    val GPR_WR_SRC_MEM = 2.U(SIGS_WIDTH.W)
    val GPR_WR_SRC_PC  = 3.U(SIGS_WIDTH.W)
    val GPR_WR_SRC_CSR = 4.U(SIGS_WIDTH.W)

    val CSR_MSTATUS = 0x300.U(ADDR_WIDTH.W)
    val CSR_MTVEC   = 0x305.U(ADDR_WIDTH.W)
    val CSR_MEPC    = 0x341.U(ADDR_WIDTH.W)
    val CSR_MCAUSE  = 0x342.U(ADDR_WIDTH.W)

    val ALU_TYPE_X     =  0.U(SIGS_WIDTH.W)
    val ALU_TYPE_ADD   =  1.U(SIGS_WIDTH.W)
    val ALU_TYPE_SUB   =  2.U(SIGS_WIDTH.W)
    val ALU_TYPE_AND   =  3.U(SIGS_WIDTH.W)
    val ALU_TYPE_OR    =  4.U(SIGS_WIDTH.W)
    val ALU_TYPE_XOR   =  5.U(SIGS_WIDTH.W)
    val ALU_TYPE_SLT   =  6.U(SIGS_WIDTH.W)
    val ALU_TYPE_SLTU  =  7.U(SIGS_WIDTH.W)
    val ALU_TYPE_SLL   =  8.U(SIGS_WIDTH.W)
    val ALU_TYPE_SLLW  =  9.U(SIGS_WIDTH.W)
    val ALU_TYPE_SRL   = 10.U(SIGS_WIDTH.W)
    val ALU_TYPE_SRLW  = 11.U(SIGS_WIDTH.W)
    val ALU_TYPE_SRLIW = 12.U(SIGS_WIDTH.W)
    val ALU_TYPE_SRA   = 13.U(SIGS_WIDTH.W)
    val ALU_TYPE_SRAW  = 14.U(SIGS_WIDTH.W)
    val ALU_TYPE_SRAIW = 15.U(SIGS_WIDTH.W)
    val ALU_TYPE_BEQ   = 16.U(SIGS_WIDTH.W)
    val ALU_TYPE_BNE   = 17.U(SIGS_WIDTH.W)
    val ALU_TYPE_BLT   = 18.U(SIGS_WIDTH.W)
    val ALU_TYPE_BGE   = 19.U(SIGS_WIDTH.W)
    val ALU_TYPE_BLTU  = 20.U(SIGS_WIDTH.W)
    val ALU_TYPE_BGEU  = 21.U(SIGS_WIDTH.W)
    val ALU_TYPE_JALR  = 22.U(SIGS_WIDTH.W)
    val ALU_TYPE_MUL   = 23.U(SIGS_WIDTH.W)
    val ALU_TYPE_DIVU  = 24.U(SIGS_WIDTH.W)
    val ALU_TYPE_DIVW  = 25.U(SIGS_WIDTH.W)
    val ALU_TYPE_DIVUW = 26.U(SIGS_WIDTH.W)
    val ALU_TYPE_REMU  = 27.U(SIGS_WIDTH.W)
    val ALU_TYPE_REMW  = 28.U(SIGS_WIDTH.W)

    val ALU_RS1_X     = 0.U(SIGS_WIDTH.W)
    val ALU_RS1_PC    = 1.U(SIGS_WIDTH.W)
    val ALU_RS1_GPR   = 2.U(SIGS_WIDTH.W)

    val ALU_RS2_X     = 0.U(SIGS_WIDTH.W)
    val ALU_RS2_GPR   = 1.U(SIGS_WIDTH.W)
    val ALU_RS2_IMM_I = 2.U(SIGS_WIDTH.W)
    val ALU_RS2_IMM_S = 3.U(SIGS_WIDTH.W)
    val ALU_RS2_IMM_B = 4.U(SIGS_WIDTH.W)
    val ALU_RS2_IMM_U = 5.U(SIGS_WIDTH.W)
    val ALU_RS2_IMM_J = 6.U(SIGS_WIDTH.W)
    val ALU_RS2_4     = 7.U(SIGS_WIDTH.W)
    val ALU_RS2_CSR   = 8.U(SIGS_WIDTH.W)

    val AXI4_RESP_OKEY   = 0.U(RESP_WIDTH.W)
    val AXI4_RESP_EXOKAY = 1.U(RESP_WIDTH.W)
    val AXI4_RESP_SLVEER = 2.U(RESP_WIDTH.W)
    val AXI4_RESP_DECEER = 3.U(RESP_WIDTH.W)

    val AXI4_MODE_RD = 0.U(MODE_WIDTH.W)
    val AXI4_MODE_WR = 1.U(MODE_WIDTH.W)
    val AXI4_MODE_RW = 2.U(MODE_WIDTH.W)

    val JMP_TR = 1.U(SIGS_WIDTH.W)
    val JMP_FL = 0.U(SIGS_WIDTH.W)

    val MEM_WR_TR = 1.U(SIGS_WIDTH.W)
    val MEM_WR_FL = 0.U(SIGS_WIDTH.W)

    val GPR_WR_TR = 1.U(SIGS_WIDTH.W)
    val GPR_WR_FL = 0.U(SIGS_WIDTH.W)

    val CSR_WR_T = 1.U(SIGS_WIDTH.W)
    val CSR_WR_F = 0.U(SIGS_WIDTH.W)
}

trait ConfigInstPattern extends ConfigInst {
    val SLL    = BitPat("b0000000_?????_?????_001_?????_01100_11")
    val SLLI   = BitPat("b000000?_?????_?????_001_?????_00100_11")
    val SRLI   = BitPat("b000000?_?????_?????_101_?????_00100_11")
    val SRA    = BitPat("b0100000_?????_?????_101_?????_01100_11")
    val SRAI   = BitPat("b010000?_?????_?????_101_?????_00100_11")
    val SLLW   = BitPat("b0000000_?????_?????_001_?????_01110_11")
    val SLLIW  = BitPat("b0000000_?????_?????_001_?????_00110_11")
    val SRLW   = BitPat("b0000000_?????_?????_101_?????_01110_11")
    val SRLIW  = BitPat("b0000000_?????_?????_101_?????_00110_11")
    val SRAW   = BitPat("b0100000_?????_?????_101_?????_01110_11")
    val SRAIW  = BitPat("b0100000_?????_?????_101_?????_00110_11")

    val ADD    = BitPat("b0000000_?????_?????_000_?????_01100_11")
    val ADDI   = BitPat("b???????_?????_?????_000_?????_00100_11")
    val SUB    = BitPat("b0100000_?????_?????_000_?????_01100_11")
    val LUI    = BitPat("b???????_?????_?????_???_?????_01101_11")
    val AUIPC  = BitPat("b???????_?????_?????_???_?????_00101_11")
    val ADDW   = BitPat("b0000000_?????_?????_000_?????_01110_11")
    val ADDIW  = BitPat("b???????_?????_?????_000_?????_00110_11")
    val SUBW   = BitPat("b0100000_?????_?????_000_?????_01110_11")

    val XOR    = BitPat("b0000000_?????_?????_100 ?????_01100_11")
    val XORI   = BitPat("b???????_?????_?????_100_?????_00100_11")
    val OR     = BitPat("b0000000_?????_?????_110_?????_01100_11")
    val ORI    = BitPat("b???????_?????_?????_110_?????_00100_11")
    val AND    = BitPat("b0000000_?????_?????_111_?????_01100_11")
    val ANDI   = BitPat("b???????_?????_?????_111_?????_00100_11")

    val SLT    = BitPat("b0000000_?????_?????_010_?????_01100_11")
    val SLTU   = BitPat("b0000000_?????_?????_011_?????_01100_11")
    val SLTIU  = BitPat("b???????_?????_?????_011_?????_00100_11")

    val BEQ    = BitPat("b???????_?????_?????_000_?????_11000_11")
    val BNE    = BitPat("b???????_?????_?????_001_?????_11000_11")
    val BLT    = BitPat("b???????_?????_?????_100_?????_11000_11")
    val BGE    = BitPat("b???????_?????_?????_101_?????_11000_11")
    val BLTU   = BitPat("b???????_?????_?????_110_?????_11000_11")
    val BGEU   = BitPat("b???????_?????_?????_111_?????_11000_11")

    val JAL    = BitPat("b???????_?????_?????_???_?????_11011_11")
    val JALR   = BitPat("b???????_?????_?????_000_?????_11001_11")

    val LB     = BitPat("b???????_?????_?????_000_?????_00000_11")
    val LH     = BitPat("b???????_?????_?????_001_?????_00000_11")
    val LBU    = BitPat("b???????_?????_?????_100_?????_00000_11")
    val LHU    = BitPat("b???????_?????_?????_101_?????_00000_11")
    val LW     = BitPat("b???????_?????_?????_010_?????_00000_11")
    val LWU    = BitPat("b???????_?????_?????_110_?????_00000_11")
    val LD     = BitPat("b???????_?????_?????_011_?????_00000_11")

    val SB     = BitPat("b???????_?????_?????_000_?????_01000_11")
    val SH     = BitPat("b???????_?????_?????_001_?????_01000_11")
    val SW     = BitPat("b???????_?????_?????_010_?????_01000_11")
    val SD     = BitPat("b???????_?????_?????_011_?????_01000_11")

    val ECALL  = BitPat("b0000000_00000_00000_000_00000_11100_11")
    val EBREAK = BitPat("b0000000_00001_00000_000_00000_11100_11")

    val CSRRW  = BitPat("b???????_?????_?????_001_?????_11100_11")
    val CSRRS  = BitPat("b???????_?????_?????_010_?????_11100_11")
    val MRET   = BitPat("b0011000_00010_00000_000_00000_11100_11")

    val MUL    = BitPat("b0000001_?????_?????_000_?????_01100_11")
    val MULW   = BitPat("b0000001_?????_?????_000_?????_01110_11")
    val DIVU   = BitPat("b0000001_?????_?????_101_?????_01100_11")
    val DIVW   = BitPat("b0000001_?????_?????_100_?????_01110_11")
    val DIVUW  = BitPat("b0000001_?????_?????_101_?????_01110_11")
    val REMU   = BitPat("b0000001_?????_?????_111_?????_01100_11")
    val REMW   = BitPat("b0000001_?????_?????_110_?????_01110_11")
}
