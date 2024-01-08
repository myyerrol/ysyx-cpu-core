package cpu.stage.pipeline

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.port._

class IFU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iJmpEn = Input(Bool())
        val iJmpPC = Input(UInt(ADDR_WIDTH.W))
        val iInst  = Input(UInt(INST_WIDTH.W))

        val pIFU   = new IFUIO
    })

    val rPC = RegInit(ADDR_INIT)
    val wPCNext = MuxCase(
        rPC + 4.U(ADDR_WIDTH.W),
        Seq(
            (io.iJmpEn === true.B) -> io.iJmpPC
        )
    )

    rPC := wPCNext

    io.pIFU.oPC   := rPC
    io.pIFU.oInst := io.iInst
}