package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class WBU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iWrSrc   =  Input(UInt(SIGS_WIDTH.W))
        val iALUOut  =  Input(UInt(DATA_WIDTH.W))
        val iMemData =  Input(UInt(DATA_WIDTH.W))

        val oWrData  = Output(UInt(DATA_WIDTH.W))
    })

    io.oWrData := MuxLookup(
        io.iWrSrc,
        DATA_ZERO,
        Seq(
            GPR_WR_SRC_ALU -> io.iALUOut,
            GPR_WR_SRC_MEM -> io.iMemData
        )
    )
}
