package cpu.core.pipeline

import chisel3._
import chisel3.util._

import cpu.common._

class IFU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val ifio = new IFIO
    })

    protected val pc     = RegInit(ADDR_INIT)
    protected val pcNext = pc + 4.U

    io.ifio.pc := pcNext
}
