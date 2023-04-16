package utils

import chisel3._
import chisel3.util._

object Base {
    val INST_WIDTH = 32
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

    // val ADDI   = 0x01
    // val AUIPC  = 0x02
    // val EBREAK = 0xFE
    // val INV    = 0xFF
}
