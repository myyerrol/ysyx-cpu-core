package cpu.common

import chisel3._
import chisel3.util._

class Reg extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iData = Input (UInt(DATA_WIDTH.W))
        val oData = Output(UInt(DATA_WIDTH.W))
    })

    val rData = RegNext(io.iData, DATA_ZERO)

    io.oData := rData
}
