package cpu.stage.single

import chisel3._
import chisel3.util._

import cpu.common._

class LSU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iMemWrEn   = Input (Bool())
        val iMemWrAddr = Input (UInt(DATA_WIDTH.W))
        val iMemWrData = Input (UInt(DATA_WIDTH.W))

        val oMemWrEn   = Output(Bool())
        val oMemWrAddr = Output(UInt(DATA_WIDTH.W))
        val oMemWrData = Output(UInt(DATA_WIDTH.W))
    })

    io.oMemWrEn   := io.iMemWrEn
    io.oMemWrAddr := io.iMemWrAddr
    io.oMemWrData := io.iMemWrData
}
