package comp

import chisel3._
import chisel3.util._

import util.Base._

class EXU extends Module {
    val io = IO(new Bundle {
        val iRegWrEn   = Input(UInt(1.U))
        val iRegWrAddr = Input(UInt(DATA_WIDTH.W))
        val iRegWrData = Input(UInt(DATA_WIDTH.W))
        val iRegRdData = Output(UInt(DATA_WIDTH.W))
    })

    val regFile = Mem(REGS_NUM, UInt(DATA_WIDTH.W))


}
