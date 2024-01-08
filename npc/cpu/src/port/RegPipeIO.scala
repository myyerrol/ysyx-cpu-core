package cpu.port

import chisel3._
import chisel3.util._

import cpu.common._

class RegPipeIO extends Bundle with ConfigIO {
        val iEn    = Input (Bool())

        val iValid = Input (Bool())
        val iReady = Input (Bool())
        val iPC    = Input (UInt(ADDR_WIDTH.W))
        val iInst  = Input (UInt(INST_WIDTH.W))

        val oValid = Output(Bool())
        val oReady = Output(Bool())
        val oPC    = Output(UInt(ADDR_WIDTH.W))
        val oInst  = Output(UInt(INST_WIDTH.W))
}
