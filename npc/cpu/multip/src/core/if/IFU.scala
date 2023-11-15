package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class IFU extends Moudule with ConfigInst {
    val io = IO(new Bundle {
        val oPC = Output(UInt(DATA_WIDTH.W))
    })

    val pc     = RegInit(ADDR_SIM_START)
    val pcNext = pc + 4.U(DATA_WIDTH.W)

    io.oPC := pcNext
}
