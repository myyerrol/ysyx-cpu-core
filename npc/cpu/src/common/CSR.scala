package cpu.common

import chisel3._
import chisel3.util._

class CSR extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iCSRWrEn       = Input (Bool())
        val iCSRWrMEn      = Input (Bool())

        val iCSRRdAddr     = Input (UInt(ADDR_WIDTH.W))
        val iCSRRdMSTAAddr = Input (UInt(ADDR_WIDTH.W))
        val iCSRRdMTVEAddr = Input (UInt(ADDR_WIDTH.W))
        val iCSRRdMEPCAddr = Input (UInt(ADDR_WIDTH.W))
        val iCSRRdMCAUAddr = Input (UInt(ADDR_WIDTH.W))

        val iCSRWrAddr     = Input (UInt(ADDR_WIDTH.W))
        val iCSRWrMEPCAddr = Input (UInt(ADDR_WIDTH.W))
        val iCSRWrMCAUAddr = Input (UInt(ADDR_WIDTH.W))
        val iCSRWrData     = Input (UInt(DATA_WIDTH.W))
        val iCSRWrMEPCData = Input (UInt(DATA_WIDTH.W))
        val iCSRWrMCAUData = Input (UInt(DATA_WIDTH.W))

        val oCSRRdData     = Output(UInt(DATA_WIDTH.W))
        val oCSRRdMSTAData = Output(UInt(DATA_WIDTH.W))
        val oCSRRdMTVEData = Output(UInt(DATA_WIDTH.W))
        val oCSRRdMEPCData = Output(UInt(DATA_WIDTH.W))
        val oCSRRdMCAUData = Output(UInt(DATA_WIDTH.W))
    })

    val csrFile = Mem(CSRS_NUM, UInt(DATA_WIDTH.W))

    when (io.iCSRWrEn) {
        csrFile(io.iCSRWrAddr) := io.iCSRWrData
    }

    when (io.iCSRWrMEn) {
        csrFile(io.iCSRWrMEPCAddr) := io.iCSRWrMEPCData
        csrFile(io.iCSRWrMCAUAddr) := io.iCSRWrMCAUData
    }

    io.oCSRRdData     := csrFile(io.iCSRRdAddr)
    io.oCSRRdMSTAData := csrFile(io.iCSRRdMSTAAddr)
    io.oCSRRdMTVEData := csrFile(io.iCSRRdMTVEAddr)
    io.oCSRRdMEPCData := csrFile(io.iCSRRdMEPCAddr)
    io.oCSRRdMCAUData := csrFile(io.iCSRRdMCAUAddr)
}
