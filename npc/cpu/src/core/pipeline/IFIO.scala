package cpu.core.pipeline

import chisel3._
import chisel3.util._

import cpu.common._

class IFIO extends Bundle with ConfigIO {
    val pc = Output(UInt(DATA_WIDTH.W))
}
