package cpu.port

import chisel3._
import chisel3.util._

import cpu.common._

class GPRIO extends Bundle with ConfigIO {
    val oRdData0  = Output(UInt(DATA_WIDTH.W))
    val oRdData1  = Output(UInt(DATA_WIDTH.W))
    val oRdData2  = Output(UInt(DATA_WIDTH.W))
    val oRdData3  = Output(UInt(DATA_WIDTH.W))
    val oRdData4  = Output(UInt(DATA_WIDTH.W))
    val oRdData5  = Output(UInt(DATA_WIDTH.W))
    val oRdData6  = Output(UInt(DATA_WIDTH.W))
    val oRdData7  = Output(UInt(DATA_WIDTH.W))
    val oRdData8  = Output(UInt(DATA_WIDTH.W))
    val oRdData9  = Output(UInt(DATA_WIDTH.W))
    val oRdData10 = Output(UInt(DATA_WIDTH.W))
    val oRdData11 = Output(UInt(DATA_WIDTH.W))
    val oRdData12 = Output(UInt(DATA_WIDTH.W))
    val oRdData13 = Output(UInt(DATA_WIDTH.W))
    val oRdData14 = Output(UInt(DATA_WIDTH.W))
    val oRdData15 = Output(UInt(DATA_WIDTH.W))
    val oRdData16 = Output(UInt(DATA_WIDTH.W))
    val oRdData17 = Output(UInt(DATA_WIDTH.W))
    val oRdData18 = Output(UInt(DATA_WIDTH.W))
    val oRdData19 = Output(UInt(DATA_WIDTH.W))
    val oRdData20 = Output(UInt(DATA_WIDTH.W))
    val oRdData21 = Output(UInt(DATA_WIDTH.W))
    val oRdData22 = Output(UInt(DATA_WIDTH.W))
    val oRdData23 = Output(UInt(DATA_WIDTH.W))
    val oRdData24 = Output(UInt(DATA_WIDTH.W))
    val oRdData25 = Output(UInt(DATA_WIDTH.W))
    val oRdData26 = Output(UInt(DATA_WIDTH.W))
    val oRdData27 = Output(UInt(DATA_WIDTH.W))
    val oRdData28 = Output(UInt(DATA_WIDTH.W))
    val oRdData29 = Output(UInt(DATA_WIDTH.W))
    val oRdData30 = Output(UInt(DATA_WIDTH.W))
    val oRdData31 = Output(UInt(DATA_WIDTH.W))
}
