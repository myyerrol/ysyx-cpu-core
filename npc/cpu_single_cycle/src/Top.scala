import chisel3._
import chisel3.util._

import cpu.comp._
import cpu.stage._
import cpu.util.Base._

class Top extends Module {
    val io = IO(new Bundle {
        val iInst      =  Input(UInt(DATA_WIDTH.W))
        val oPC        = Output(UInt(DATA_WIDTH.W))
    });

    val ifu = Module(new IFU())
    val pc = RegInit("x80000000".U(64.W))
    ifu.io.iPC := pc
    io.oPC := ifu.io.oPC

    val reg = Module(new Reg())

    val idu = Module(new IDU())
    idu.io.iInst := io.iInst
    idu.io.iALURS1Val := reg.io.oRegRdData
    idu.io.iALURS2Val := reg.io.oRegRdData

    reg.io.iRegRdAddr := idu.io.oInstRS1Addr
    reg.io.iRegRdAddr := idu.io.oInstRS2Addr

    val exu = Module(new EXU())
    exu.io.iInstRDAddr := idu.io.oInstRDAddr
    exu.io.iALUType    := idu.io.oALUType
    exu.io.iALURS1Val  := idu.io.oALURS1Val
    exu.io.iALURS2Val  := idu.io.oALURS2Val
    exu.io.iMemWrEn    := idu.io.oMemWrEn
    exu.io.iRegWrEn    := idu.io.oRegWrEn
    exu.io.iCsrWrEn    := idu.io.oCsrWrEn

    // val dpi = Module(new DPI())
    // dpi.io.i_ebreak_flag := Mux(idu.io.oInstType === EBREAK.U, 1.U, 0.U)

    printf("pc:   0x%x\n", io.oPC)
    printf("inst: 0x00000000%x\n", io.iInst)

    pc := pc + 4.U



    // val io = IO(new Bundle {
    //     val iInst      =  Input(UInt(32.W))
    //     val oPC        = Output(UInt(64.W))
    //     val oInstRDVal = Output(UInt(64.W))
    //     val oHalt      = Output(Bool())
    // });

    // val ifu = Module(new IFU())
    // val pc = RegInit("x80000000".U(64.W))
    // ifu.io.iPC := pc
    // io.oPC := ifu.io.oPC

    // val idu = Module(new IDU())
    // idu.io.iInst := io.iInst

    // val exu = Module(new EXU())
    // exu.io.iInstType := idu.io.oInstType
    // exu.io.iInstRS1  := idu.io.oInstRS1
    // exu.io.iInstRS2  := idu.io.oInstRS2
    // exu.io.iInstRD   := idu.io.oInstRD
    // exu.io.iInstImm  := idu.io.oInstImm

    // io.oInstRDVal := exu.io.oInstRDVal
    // io.oHalt      := exu.io.oHalt

    // val dpi = Module(new DPI())
    // dpi.io.i_ebreak_flag := Mux(idu.io.oInstType === EBREAK.U, 1.U, 0.U)

    // printf("pc:   0x%x\n", io.oPC)
    // printf("inst: 0x00000000%x\n", io.iInst)
    // printf("res:  0x%x\n", io.oInstRDVal)
    // printf("halt: %x\n\n", io.oHalt)

    // pc := pc + 4.U
}
