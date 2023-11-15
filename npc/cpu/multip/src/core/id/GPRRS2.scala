package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class GPRRS2 extends Module with ConfigInst {
    val io = IO(New Bundle {
        val iRS2Data =  Input(UInt(DATA_WIDTH.W))
        val oRS2Data = Output(UInt(DATA_WIDTH.W))
    })

    io.oRS2Data := RegNext(io.iRS2Data)
}
