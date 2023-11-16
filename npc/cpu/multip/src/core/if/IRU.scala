package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class IRU extends Module with ConfigInst {
   val io = IO(new Bundle {
        val iIREn =  Input(Bool())
        val iInst =  Input(UInt(DATA_WIDTH.W))

        val oInst = Output(UInt(DATA_WIDTH.W))
    })

    val rInst = RegEnable(io.iInst, DATA_ZERO, io.iIREn === SIG_TRUE)

    io.oInst := rInst
}
