package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class MRU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val dataI =  Input(UInt(DATA_WIDTH.W))
        val dataO = Output(UInt(DATA_WIDTH.W))
    })

    val data = RegNext(io.dataI, DATA_ZERO)

    io.dataO := data
}
