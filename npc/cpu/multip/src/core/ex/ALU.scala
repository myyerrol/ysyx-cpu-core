package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class ALU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iType    =  Input(UInt(SIGS_WIDTH.W))
        val iRS1Data =  Input(UInt(DATA_WIDTH.W))
        val iRS2Data =  Input(UInt(DATA_WIDTH.W))

        val oZero    = Output(Bool())
        val oOut     = Output(UInt(DATA_WIDTH.W))
    })

    val jalrMask = Cat(Fill(DATA_WIDTH - 1, 1.U(1.W)), 0.U(1.U))
    val wOut = MuxLookup(
        io.iType,
        DATA_ZERO,
        Seq(
            ALU_TYPE_ADD  ->  (io.iRS1Data + io.iRS2Data),
            ALU_TYPE_JALR -> ((io.iRS1Data + io.iRS2Data) & jalrMask)
        )
    )

    io.oZero := Mux(wOut === DATA_ZERO, EN_TRUE, EN_FALSE)
    io.oOut  := wOut
}
