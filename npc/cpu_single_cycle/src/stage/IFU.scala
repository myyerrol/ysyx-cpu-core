package cpu.stage

import chisel3._
import chisel3.util._

import cpu.util.Base._

class IFU extends Module {
    val io = IO(new Bundle {
        val iJmpEn = Input(Bool())
        val iPC    = Input(UInt(DATA_WIDTH.W))
        val oPC    = Output(UInt(DATA_WIDTH.W))
    })

    val pc = RegInit("x80000000".U(DATA_WIDTH.W))
    val pcNext = MuxCase(
        pc + 4.U(DATA_WIDTH.W),
        Seq(
            (io.iJmpEn === true.B) -> io.iPC
        )
    )
    pc := pcNext

    io.oPC := pc
}