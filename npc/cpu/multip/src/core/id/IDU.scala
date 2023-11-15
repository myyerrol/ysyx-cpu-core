package cpu.core

import chisel3._
import chisel3.util._

import cpu.core._

class IDU extends Module with ConfigInstPattern {
    val IO = IO(new Bundle {
        val iPC   = Input(UInt(DATA_WIDTH.W))
        val iInst = Input(UInt(DATA_WIDTH.W))
    })



}
