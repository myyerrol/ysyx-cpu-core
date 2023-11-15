package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class IRU extends Moudule with ConfigInst {
   val io = IO(new Bundle {
        val iEnIR  =  Input(Bool())
        val iInst =  Input(UInt(INST_WIDTH.W))
        val oInst = Output(UInt(INST_WIDTH.W))
    })

    val inst = RegEnable(io.iInst, DATA_ZERO, iEnIR)

    io.oInst := inst
}
