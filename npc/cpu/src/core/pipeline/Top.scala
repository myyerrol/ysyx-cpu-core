package cpu.core.pipeline

import chisel3._
import chisel3.util._

import cpu.core._

class Top extends Module {
    // val ifu     = Module(new IFU)
    val ifu2idu = Module(new IFU2IDU)
    ifu2idu.io.ifio.pc := 1.U
    ifu2idu.io.inst    := 1.U
    // ifu.io.ifio <> ifu2idu.io.ifio
}