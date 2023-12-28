package cpu.bus

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.mem._
import cpu.port._

class AXI4LiteArbiter extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iState   = Input(UInt(SIGS_WIDTH.W))

        val pAXI4IFU = Flipped(new AXI4LiteIO)
        val pAXI4LSU = Flipped(new AXI4LiteIO)
    })

    val mAXI4LiteSRAM = Module(new AXI4LiteSRAM)
    mAXI4LiteSRAM.io.iState := io.iState

    val pAXI4IFUReverse = Wire(new AXI4LiteIO)
    val pAXI4LSUReverse = Wire(new AXI4LiteIO)
    pAXI4IFUReverse := DontCare
    pAXI4LSUReverse := DontCare

    pAXI4IFUReverse <> io.pAXI4IFU
    pAXI4LSUReverse <> io.pAXI4LSU

    when (io.iState === STATE_IF) {
        mAXI4LiteSRAM.io.pAXI4 <> pAXI4IFUReverse
    }
    .elsewhen (io.iState === STATE_LS) {
        mAXI4LiteSRAM.io.pAXI4 <> pAXI4LSUReverse
    }
    .otherwise {
        mAXI4LiteSRAM.io.pAXI4 <> pAXI4IFUReverse
    }
}
