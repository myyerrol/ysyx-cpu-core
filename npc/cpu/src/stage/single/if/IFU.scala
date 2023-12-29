package cpu.stage.single

import chisel3._
import chisel3.util._

import cpu.common._

class IFU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iJmpEn = Input (Bool())
        val iJmpPC = Input (UInt(ADDR_WIDTH.W))

        val oPC    = Output(UInt(ADDR_WIDTH.W))
    })

    val rPC = RegInit(ADDR_INIT)
    val wPCNext = MuxCase(
        rPC + 4.U(ADDR_WIDTH.W),
        Seq(
            (io.iJmpEn === true.B) -> io.iJmpPC
        )
    )

    rPC    := wPCNext
    io.oPC := rPC
}
