package cpu.port

import chisel3._
import chisel3.util._

import cpu.common._

class IDUIO extends Bundle with ConfigIO {
    val oRS1Addr = Output(UInt(DATA_WIDTH.W))
    val oRS2Addr = Output(UInt(DATA_WIDTH.W))
    val oRDAddr  = Output(UInt(DATA_WIDTH.W))
    val oRS1Data = Output(UInt(DATA_WIDTH.W))
    val oRS2Data = Output(UInt(DATA_WIDTH.W))
    val oEndData = Output(UInt(DATA_WIDTH.W))
    val oImmData = Output(UInt(DATA_WIDTH.W))
}
