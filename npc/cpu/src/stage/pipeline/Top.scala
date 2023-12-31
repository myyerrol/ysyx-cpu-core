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

    val mSysDPIDirect = Module(new SysDPIDirect)

    val mIFU = Module(new IFU)
    val mIDU = Module(new IDU)
    val mEXU = Module(new EXU)
    val mLSU = Module(new LSU)
    val mWBU = Module(new WBU)

    val mIFU2IDU = Module(new IFU2IDU)
    val mIDU2EXU = Module(new IDU2EXU)
    val mEXU2LSU = Module(new EXU2LSU)
    val mLSU2WBU = Module(new LSU2WBU)

    io.oEndData := 0.U

    io.pIFU    <> DontCare
    io.pGPR    <> DontCare
    io.pITrace <> DontCare

    mIFU2IDU.io.iEn    := true.B
    mIFU2IDU.io.iValid := true.B
    mIFU2IDU.io.iPC   := mIFU.io.pIFU.oPC
    mIFU2IDU.io.iInst := DontCare


}