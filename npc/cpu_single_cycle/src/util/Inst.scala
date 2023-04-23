package cpu.util

import chisel3._
import chisel3.util._

object Inst {
    // 移位指令
    val SLLI   = BitPat("b000000_??????_?????_001_?????_00100_11")
    val SRLI   = BitPat("b000000_??????_?????_101_?????_00100_11")
    val SRA    = BitPat("b0100000_?????_?????_101_?????_01100_11")
    val SRAI   = BitPat("b010000_??????_?????_101_?????_00100_11")
    val SLLW   = BitPat("b0000000_?????_?????_001_?????_01110_11")
    val SLLIW  = BitPat("b000000_??????_?????_001_?????_00110_11")
    val SRLW   = BitPat("b000000_??????_?????_101_?????_01110_11")
    val SRLIW  = BitPat("b000000_??????_?????_101_?????_00110_11")
    val SRAW   = BitPat("b0100000_?????_?????_101_?????_01110_11")
    val SRAIW  = BitPat("b0100000_?????_?????_101_?????_00110_11")
    // 算术指令
    val ADD    = BitPat("b0000000_?????_?????_000_?????_01100_11")
    val ADDI   = BitPat("b???????_?????_?????_000_?????_00100_11")
    val SUB    = BitPat("b0100000_?????_?????_000_?????_01100_11")
    val LUI    = BitPat("b???????_?????_?????_???_?????_01101_11")
    val AUIPC  = BitPat("b???????_?????_?????_???_?????_00101_11")
    val ADDW   = BitPat("b0000000_?????_?????_000_?????_01110_11")
    val ADDIW  = BitPat("b???????_?????_?????_000_?????_00110_11")
    val SUBW   = BitPat("b0100000_?????_?????_000_?????_01110_11")
    // 逻辑指令
    val XORI   = BitPat("b???????_?????_?????_100_?????_00100_11")
    val OR     = BitPat("b0000000_?????_?????_110_?????_01100_11")
    val AND    = BitPat("b0000000_?????_?????_111_?????_01100_11")
    val ANDI   = BitPat("b???????_?????_?????_111_?????_00100_11")
    // 比较指令
    val SLT    = BitPat("b0000000_?????_?????_010_?????_01100_11")
    val SLTU   = BitPat("b0000000_?????_?????_011_?????_01100_11")
    val SLTIU  = BitPat("b???????_?????_?????_011_?????_00100_11")
    // 分支指令
    val BEQ    = BitPat("b???????_?????_?????_000_?????_11000_11")
    val BNE    = BitPat("b???????_?????_?????_001_?????_11000_11")
    val BLT    = BitPat("b???????_?????_?????_100_?????_11000_11")
    val BGE    = BitPat("b???????_?????_?????_101_?????_11000_11")
    val BLTU   = BitPat("b???????_?????_?????_110_?????_11000_11")
    val BGEU   = BitPat("b???????_?????_?????_111_?????_11000_11")
    // 跳转指令
    val JAL    = BitPat("b???????_?????_?????_???_?????_11011_11")
    val JALR   = BitPat("b???????_?????_?????_000_?????_11001_11")
    // 加载指令
    val LB     = BitPat("b???????_?????_?????_000_?????_00000_11")
    val LH     = BitPat("b???????_?????_?????_001_?????_00000_11")
    val LBU    = BitPat("b???????_?????_?????_100_?????_00000_11")
    val LHU    = BitPat("b???????_?????_?????_101_?????_00000_11")
    val LW     = BitPat("b???????_?????_?????_010_?????_00000_11")
    val LD     = BitPat("b???????_?????_?????_011_?????_00000_11")
    // 存储指令
    val SB     = BitPat("b???????_?????_?????_000_?????_01000_11")
    val SH     = BitPat("b???????_?????_?????_001_?????_01000_11")
    val SW     = BitPat("b???????_?????_?????_010_?????_01000_11")
    val SD     = BitPat("b???????_?????_?????_011_?????_01000_11")
    // 环境指令
    val EBREAK = BitPat("b0000000_00001_00000_000_00000_11100_11")
    // 乘法指令
    val MUL    = BitPat("b0000001_?????_?????_000_?????_01100_11")
    val MULW   = BitPat("b0000001_?????_?????_000_?????_01110_11")
    // 除法指令
    val DIVU   = BitPat("b0000001_?????_?????_101_?????_01100_11")
    val DIVW   = BitPat("b0000001_?????_?????_100_?????_01110_11")
    // 取余指令
    val REMU   = BitPat("b0000001_?????_?????_111_?????_01100_11")
    val REMW   = BitPat("b0000001_?????_?????_110_?????_01110_11")
}
