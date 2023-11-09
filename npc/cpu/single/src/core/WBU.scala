package cpu.core

import chisel3._
import chisel3.util._

import cpu.common.Base._

class WBU extends Module {
    val io = IO(new Bundle {
        val iRegWrEn   = Input(Bool())
        val iRegWrAddr = Input(UInt(DATA_WIDTH.W))
        val iRegWrData = Input(UInt(DATA_WIDTH.W))

        val oRegWrEn   = Output(Bool())
        val oRegWrAddr = Output(UInt(DATA_WIDTH.W))
        val oRegWrData = Output(UInt(DATA_WIDTH.W))
    })

    io.oRegWrEn   := io.iRegWrEn
    io.oRegWrAddr := io.iRegWrAddr
    io.oRegWrData := io.iRegWrData
}
