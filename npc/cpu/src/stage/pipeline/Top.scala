package cpu.stage.pipeline

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.port._

class Top extends Module with ConfigInst {
    val io = IO(new Bundle {
        val oEndData  = Output(UInt(DATA_WIDTH.W))

        val pIFU      = new IFUIO
        val pGPR      = new GPRIO
        val pITrace   = new ITraceIO
    });

    io.oEndData := 0.U

    io.pIFU    <> DontCare
    io.pGPR    <> DontCare
    io.pITrace <> DontCare
}