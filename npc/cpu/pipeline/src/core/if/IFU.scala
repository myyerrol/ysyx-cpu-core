package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.io._

class IFU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val ifio = new IFIO
    })

    protected val pc     = RegInit(ADDR_SIM)
    protected val pcNext = pc + 4.U

    io.ifio.pc := pcNext
}
