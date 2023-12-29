package cpu.core.multip

import chisel3._
import chisel3.util._

import cpu.common._

class IRU extends Module with ConfigInst {
   val io = IO(new Bundle {
        val iWrEn = Input (Bool())
        val iData = Input (UInt(INST_WIDTH.W))

        val oData = Output(UInt(INST_WIDTH.W))
    })

    val rData = RegEnable(io.iData, DATA_ZERO, io.iWrEn)

    io.oData := rData
}
