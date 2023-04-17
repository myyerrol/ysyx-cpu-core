package dpi

import chisel3._;
import chisel3.util._

class DPI extends BlackBox {
    val io = IO(new Bundle {
        val i_ebreak_flag = Input(UInt(32.W))
    })
}
