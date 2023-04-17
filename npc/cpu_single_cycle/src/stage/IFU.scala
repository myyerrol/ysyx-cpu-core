package cpu.stage

import chisel3._
import chisel3.util._

import cpu.util.Base._

class IFU extends Module {
    val io = IO(new Bundle {
        val iPC =  Input(UInt(DATA_WIDTH.W))
        val oPC = Output(UInt(DATA_WIDTH.W))
    })

    io.oPC := io.iPC
}
