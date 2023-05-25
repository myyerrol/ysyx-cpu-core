package cpu.comp

import chisel3._
import chisel3.util._

import cpu.util.Base._

class CSR extends Module {
    val io = IO(new Bundle {
        val iCSRWrEn       = Input(Bool())
        val iCSRWrMEn      = Input(Bool())
        val iCSRRdAddr     = Input(UInt(DATA_WIDTH.W))

        val iCSRWrAddr     = Input(UInt(DATA_WIDTH.W))
        val iCSRWrMEPCAddr = Input(UInt(DATA_WIDTH.W))
        val iCSRWrMCAUAddr = Input(UInt(DATA_WIDTH.W))
        val iCSRWrData     = Input(UInt(DATA_WIDTH.W))
        val iCSRWrMEPCData = Input(UInt(DATA_WIDTH.W))
        val iCSRWrMCAUData = Input(UInt(DATA_WIDTH.W))

        val oCSRRdData = Output(UInt(DATA_WIDTH.W))
    })

    val csrFile = Mem(CSR_NUM, UInt(DATA_WIDTH.W))

    when (io.iCSRWrEn) {
        csrFile(io.iCSRWrAddr) := io.iCSRWrData
    }

    when (io.iCSRWrMEn) {
        csrFile(io.iCSRWrMEPCAddr) := io.iCSRWrMEPCData
        csrFile(io.iCSRWrMCAUAddr) := io.iCSRWrMCAUData
    }

    io.oCSRRdData := csrFile(io.iCSRRdAddr)
}
