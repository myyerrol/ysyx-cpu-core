package cpu.stage.pipeline

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.port._

class IFU2IDU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val pRegPipe = new RegPipeIO
    })

    val rValid = RegEnable(io.pRegPipe.iValid, false.B,   io.pRegPipe.iEn)
    val rReady = RegEnable(io.pRegPipe.iReady, false.B,   io.pRegPipe.iEn)
    val rPC    = RegEnable(io.pRegPipe.iPC,    ADDR_ZERO, io.pRegPipe.iEn)
    val rInst  = RegEnable(io.pRegPipe.iInst,  INST_ZERO, io.pRegPipe.iEn)

    io.pRegPipe.oValid := rValid
    io.pRegPipe.oReady := rReady
    io.pRegPipe.oPC    := rPC
    io.pRegPipe.oInst  := rInst
}
