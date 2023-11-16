package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class IFU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val oPC = Output(UInt(DATA_WIDTH.W))
    })

    val rPC = RegInit(ADDR_SIM_START)

    io.oPC := rPC
}
