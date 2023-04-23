import chisel3._
import chisel3.util._

import cpu.comp._
import cpu.dpi._
import cpu.stage._
import cpu.util.Base._
import cpu.util.Inst._

class Top extends Module {
    val io = IO(new Bundle {
        val iInst =  Input(UInt(DATA_WIDTH.W))
        val oPC   = Output(UInt(DATA_WIDTH.W))
        val oReg  = Output(UInt(DATA_WIDTH.W))
    });

    val ifu = Module(new IFU())
    io.oPC := ifu.io.oPC
    // io.oPC := ifu.io.oPCNext

    val mem = Module(new MemM())
    mem.io.iMemRdAddr := 0.U(DATA_WIDTH.W)

    val reg = Module(new RegM())

    val idu = Module(new IDU())
    idu.io.iInst       := io.iInst
    idu.io.iInstRS1Val := reg.io.oRegRd1Data
    idu.io.iInstRS2Val := reg.io.oRegRd2Data
    idu.io.iPC         := ifu.io.oPC

    reg.io.iRegRd1Addr := idu.io.oInstRS1Addr
    reg.io.iRegRd2Addr := idu.io.oInstRS2Addr

    val exu = Module(new EXU())
    exu.io.iInstRS1Addr := idu.io.oInstRS1Addr
    exu.io.iInstRS2Addr := idu.io.oInstRS2Addr
    exu.io.iInstRDAddr  := idu.io.oInstRDAddr
    exu.io.iInstRS1Val  := idu.io.oInstRS1Val
    exu.io.iInstRS2Val  := idu.io.oInstRS2Val
    exu.io.iPC          := ifu.io.oPC
    exu.io.iALUType     := idu.io.oALUType
    exu.io.iALURS1Val   := idu.io.oALURS1Val
    exu.io.iALURS2Val   := idu.io.oALURS2Val
    exu.io.iJmpEn       := idu.io.oJmpEn
    exu.io.iMemWrEn     := idu.io.oMemWrEn
    exu.io.iRegWrEn     := idu.io.oRegWrEn
    exu.io.iRegWrSrc    := idu.io.oRegWrSrc
    exu.io.iCsrWrEn     := idu.io.oCsrWrEn

    val amu = Module(new AMU())
    amu.io.iMemWrEn   := exu.io.oMemWrEn
    amu.io.iMemWrAddr := exu.io.oMemWrAddr
    amu.io.iMemWrData := exu.io.oMemWrData

    mem.io.iMemWrEn   := amu.io.oMemWrEn
    mem.io.iMemWrAddr := amu.io.oMemWrAddr
    mem.io.iMemWrData := amu.io.oMemWrData

    val wbu = Module(new WBU())
    ifu.io.iJmpEn     := exu.io.oJmpEn
    ifu.io.iJmpPC     := exu.io.oJmpPC
    wbu.io.iRegWrEn   := exu.io.oRegWrEn
    wbu.io.iRegWrAddr := exu.io.oRegWrAddr
    wbu.io.iRegWrData := exu.io.oRegWrData

    reg.io.iRegWrEn   := wbu.io.oRegWrEn
    reg.io.iRegWrAddr := wbu.io.oRegWrAddr
    reg.io.iRegWrData := wbu.io.oRegWrData

    val dpi = Module(new DPI())
    dpi.io.iEbreakFlag := Mux(idu.io.oALUType === ALU_TYPE_EBREAK, 1.U, 0.U)
    dpi.io.iRegVal := 11.U
    printf("reg addr: %d\n", dpi.io.oRegAddr)
    reg.io.iRegRdEAddr := 10.U(REG_WIDTH.W)
    io.oReg := reg.io.oRegRdEData

    printf("pc:          0x%x\n", io.oPC)
    printf("inst:        0x%x\n", io.iInst)

    val instName = idu.io.oInstName
    switch (instName) {
        is (INST_NAME_LUI)    { printf(p"inst name:   LUI\n") }
        is (INST_NAME_AUIPC)  { printf(p"inst name:   AUIPC\n") }
        is (INST_NAME_JAL)    { printf(p"inst name:   JAL\n") }
        is (INST_NAME_JALR)   { printf(p"inst name:   JALR\n") }
        is (INST_NAME_SD)     { printf(p"inst name:   SD\n")}
        is (INST_NAME_ADDI)   { printf(p"inst name:   ADDI\n") }
        is (INST_NAME_EBREAK) { printf(p"inst name:   EBREAK\n") }
    }
    printf("rs1 addr:   %d\n", idu.io.oInstRS1Addr)
    printf("rs2 addr:   %d\n", idu.io.oInstRS2Addr)
    printf("rd  addr:   %d\n", idu.io.oInstRDAddr)
    printf("rs1 val :    0x%x\n", idu.io.iInstRS1Val)
    printf("rs2 val :    0x%x\n", idu.io.iInstRS2Val)
    printf("rd  val :    0x%x\n", exu.io.oRegWrData)
    printf("alu type: %d\n", idu.io.oALUType)
    printf("alu rs1 val: 0x%x\n", idu.io.oALURS1Val)
    printf("alu rs2 val: 0x%x\n", idu.io.oALURS2Val)
    printf("alu out:     0x%x\n", exu.io.oALUOut)
    printf("jmp en:      %d\n", idu.io.oJmpEn)
    printf("mem wr en:   %d\n", idu.io.oMemWrEn)
    printf("reg wr en:   %d\n", idu.io.oRegWrEn)
    printf("csr wr en:   %d\n", idu.io.oCsrWrEn)
    printf("reg(10):     0x%x\n\n", io.oReg)
}
