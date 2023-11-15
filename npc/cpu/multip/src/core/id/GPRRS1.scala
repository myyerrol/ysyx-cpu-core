package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class GPRRS1 extends Module with ConfigInst {
    val io = IO(New Bundle {
        val iRS1Data =  Input(UInt(DATA_WIDTH.W))
        val oRS1Data = Output(UInt(DATA_WIDTH.W))
    })

    io.oRS1Data := RegNext(io.iRS1Data)
}
