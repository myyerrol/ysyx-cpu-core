package cpu.io

import chisel3._
import chisel3.util._

import cpu.common._

class IF2IDIO extends Bundle with ConfigIO {
    val pc   = Output(UInt(DATA_WIDTH.W))
    val inst = Output(UInt(INST_WIDTH.W))
}
