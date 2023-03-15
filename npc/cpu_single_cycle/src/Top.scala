import chisel3._
import chisel3.util._

import utils.Base._

class Top extends Module {
    val io = IO(new Bundle {
        val iInst      =  Input(UInt(32.W))
        val oPC        = Output(UInt(64.W))
        val oInstRDVal = Output(UInt(64.W))
        val oHalt      = Output(Bool())
    });

    val ifu = Module(new IFU())
    val pc = RegInit("x80000000".U(64.W))
    ifu.io.iPC := pc
    io.oPC := ifu.io.oPC

    val idu = Module(new IDU())
    idu.io.iInst := io.iInst

    val exu = Module(new EXU())
    exu.io.iInstType := idu.io.oInstType
    exu.io.iInstRS1  := idu.io.oInstRS1
    exu.io.iInstRS2  := idu.io.oInstRS2
    exu.io.iInstRD   := idu.io.oInstRD
    exu.io.iInstImm  := idu.io.oInstImm

    io.oInstRDVal := exu.io.oInstRDVal
    io.oHalt      := exu.io.oHalt

    val dpi = Module(new DPI())
    dpi.io.i_ebreak_flag := Mux(idu.io.oInstType === EBREAK.U, 1.U, 0.U)

    printf("pc:   0x%x\n", io.oPC)
    printf("inst: 0x00000000%x\n", io.iInst)
    printf("res:  0x%x\n", io.oInstRDVal)
    printf("halt: %x\n\n", io.oHalt)

    pc := pc + 4.U
}
