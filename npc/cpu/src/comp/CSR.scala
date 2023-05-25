package cpu.comp

import chisel3._
import chisel3.util._

import cpu.util.Base._

class CSR extends Module {
    val io = IO(new Bundle {
        val iCSRWrEn   = Input(Bool())
        val iCSRRdAddr = Input(UInt(DATA_WIDTH.W))
        val iCSRWrAddr = Input(UInt(DATA_WIDTH.W))
        val iCSRWrData = Input(UInt(DATA_WIDTH.W))

        val oCSRRdData = Output(UInt(DATA_WIDTH.W))
    })

    val csrFile = Mem(CSR_NUM, UInt(DATA_WIDTH.W))

    when (io.iCSRWrEn) {
        csrFile(io.iCSRWrAddr) := io.iCSRWrData
    }

    io.oCSRRdData := csrFile(io.iCSRRdAddr)
}
