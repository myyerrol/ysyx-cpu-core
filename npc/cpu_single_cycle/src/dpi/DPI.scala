package cpu.dpi

import chisel3._;
import chisel3.util._

class DPI extends BlackBox {
    val io = IO(new Bundle {
        val iEbreakFlag = Input(UInt(32.W))
    })
}
