package cpu.common

import chisel3._
import chisel3.util._

trait ConfigIO {
    val INST_WIDTH = 32
    val DATA_WIDTH = 64
}

trait ConfigInst extends ConfigIO {
    val ADDR_SIM = "x80000000".U(DATA_WIDTH.W)
    val DATA_ZERO      = "x00000000".U(DATA_WIDTH.W)
}
