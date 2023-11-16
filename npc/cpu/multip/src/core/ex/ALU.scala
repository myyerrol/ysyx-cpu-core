package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class ALU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iALUType =  Input(UInt(SIGS_WIDTH.W))
        val iRS1Data =  Input(UInt(DATA_WIDTH.W))
        val iRS2Data =  Input(UInt(DATA_WIDTH.W))

        val oZero    = Output(Bool())
        val oALUOut  = Output(UInt(DATA_WIDTH.W))
    })

    val wALUOut = MuxLookup(
        io.iALUType,
        DATA_ZERO,
        Seq(
            ALU_TYPE_ADD -> (io.iRS1Data + io.iRS2Data)
        )
    )

    io.oZero   := Mux(wALUOut === DATA_ZERO, EN_TRUE, EN_FALSE)
    io.oALUOut := wALUOut
}
