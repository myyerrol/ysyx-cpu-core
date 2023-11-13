package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.io._

class IFU2IDU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val ifio    = Flipped(new IFIO)
        val inst    = Input(UInt(INST_WIDTH.W))
        val if2idio =        new IF2IDIO
    })

    val pc   = RegNext(io.ifio.pc, DATA_ZERO)
    val inst = RegNext(io.inst,    DATA_ZERO)

    io.if2idio.pc   := pc
    io.if2idio.inst := inst
}
