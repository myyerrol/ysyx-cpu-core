package cpu.common

import chisel3._
import chisel3.util._

class Reg extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iEn   = Input (Bool())
        val iData = Input (UInt(DATA_WIDTH.W))

        val oData = Output(UInt(DATA_WIDTH.W))
    })

    val rData = RegEnable(io.iData, DATA_ZERO, io.iEn)

    io.oData := rData
}
