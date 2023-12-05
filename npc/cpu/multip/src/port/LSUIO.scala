package cpu.port

import chisel3._
import chisel3.util._

import cpu.common._

class LSUIO extends Bundle with ConfigIO {
    val oMemRdEn       = Output(Bool())
    val oMemRdAddrInst = Output(UInt(DATA_WIDTH.W))
    val oMemRdAddrLoad = Output(UInt(DATA_WIDTH.W))
    val oMemWrEn       = Output(Bool())
    val oMemWrAddr     = Output(UInt(DATA_WIDTH.W))
    val oMemWrData     = Output(UInt(DATA_WIDTH.W))
    val oMemWrLen      = Output(UInt(BYTE_WIDTH.W))

    val oMemRdDataInst = Output(UInt(DATA_WIDTH.W))
    val oMemRdDataLoad = Output(UInt(DATA_WIDTH.W))
    val oMemRdData     = Output(UInt(DATA_WIDTH.W))
}
