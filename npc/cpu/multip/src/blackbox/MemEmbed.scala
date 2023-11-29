package cpu.blackbox

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.core._

class MemPortDualIO extends Bundle with ConfigIO {
    val iRdEn   =  Input(Bool())
    val iWrEn   =  Input(Bool())
    val iAddr   =  Input(UInt(DATA_WIDTH.W))
    val iWrData =  Input(UInt(DATA_WIDTH.W))
    val iWrByt  =  Input(UInt(SIGS_WIDTH.W))

    val oRdData = Output(UInt(DATA_WIDTH.W))
}

class MemEmbed extends BlackBox with ConfigInst {
    val io = IO(new Bundle {
        val iClock         = Input(Clock())
        val iReset         = Input(Bool())
        val bMemPortDualIO = new MemPortDualIO
    })
}
