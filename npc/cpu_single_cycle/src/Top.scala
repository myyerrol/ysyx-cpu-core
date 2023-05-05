import chisel3._
import chisel3.util._

import cpu.comp._
import cpu.dpi._
import cpu.stage._
import cpu.util.Base._
import cpu.util.Inst._

class Top extends Module {
    val io = IO(new Bundle {
        val oPC  = Output(UInt(DATA_WIDTH.W))
        val oReg = Output(UInt(DATA_WIDTH.W))
    });

    val dpi = Module(new DPI())

    val ifu = Module(new IFU())
    io.oPC := ifu.io.oPC
    dpi.io.iMemRdAddrInst := ifu.io.oPC

    val reg = Module(new RegM())

    val idu = Module(new IDU())
    idu.io.iInst       := dpi.io.oMemRdDataInst
    idu.io.iInstRS1Val := reg.io.oRegRd1Data
    idu.io.iInstRS2Val := reg.io.oRegRd2Data
    idu.io.iPC         := ifu.io.oPC

    reg.io.iRegRd1Addr := idu.io.oInstRS1Addr
    reg.io.iRegRd2Addr := idu.io.oInstRS2Addr

    dpi.io.iEbreakFlag := Mux(idu.io.oInstName === INST_NAME_EBREAK, 1.U, 0.U)

    val exu = Module(new EXU())
    exu.io.iInstRS1Addr := idu.io.oInstRS1Addr
    exu.io.iInstRS2Addr := idu.io.oInstRS2Addr
    exu.io.iInstRDAddr  := idu.io.oInstRDAddr
    exu.io.iInstRS1Val  := idu.io.oInstRS1Val
    exu.io.iInstRS2Val  := idu.io.oInstRS2Val
    exu.io.iPC          := ifu.io.oPC
    exu.io.iMemRdData   := dpi.io.oMemRdDataLoad
    exu.io.iInstName    := idu.io.oInstName
    exu.io.iALUType     := idu.io.oALUType
    exu.io.iALURS1Val   := idu.io.oALURS1Val
    exu.io.iALURS2Val   := idu.io.oALURS2Val
    exu.io.iJmpEn       := idu.io.oJmpEn
    exu.io.iMemWrEn     := idu.io.oMemWrEn
    exu.io.iMemByt      := idu.io.oMemByt
    exu.io.iRegWrEn     := idu.io.oRegWrEn
    exu.io.iRegWrSrc    := idu.io.oRegWrSrc

    dpi.io.iMemRdAddrLoad := exu.io.oMemRdAddr

    dpi.io.iMemWrEn   := exu.io.oMemWrEn
    dpi.io.iMemWrAddr := exu.io.oMemWrAddr
    dpi.io.iMemWrData := exu.io.oMemWrData
    dpi.io.iMemWrLen  := exu.io.oMemWrLen

    val wbu = Module(new WBU())
    ifu.io.iJmpEn     := exu.io.oJmpEn
    ifu.io.iJmpPC     := exu.io.oJmpPC
    wbu.io.iRegWrEn   := exu.io.oRegWrEn
    wbu.io.iRegWrAddr := exu.io.oRegWrAddr
    wbu.io.iRegWrData := exu.io.oRegWrData

    reg.io.iRegWrEn   := wbu.io.oRegWrEn
    reg.io.iRegWrAddr := wbu.io.oRegWrAddr
    reg.io.iRegWrData := wbu.io.oRegWrData

    reg.io.iRegRdEAddr := 10.U(REG_WIDTH.W)
    io.oReg := reg.io.oRegRdEData

    printf("v pc:          0x%x\n", ifu.io.oPC)
    printf("v inst:        0x%x\n", idu.io.iInst)
    val instName = idu.io.oInstName
    switch (instName) {
        is(INST_NAME_SLLI  ) { printf(p"v inst name:   SLLI\n") }
        is(INST_NAME_SRLI  ) { printf(p"v inst name:   SRLI\n") }
        is(INST_NAME_SRA   ) { printf(p"v inst name:   SRA\n") }
        is(INST_NAME_SRAI  ) { printf(p"v inst name:   SRAI\n") }
        is(INST_NAME_SLLW  ) { printf(p"v inst name:   SLLW\n") }
        is(INST_NAME_SLLIW ) { printf(p"v inst name:   SLLIW\n") }
        is(INST_NAME_SRLW  ) { printf(p"v inst name:   SRLW\n") }
        is(INST_NAME_SRLIW ) { printf(p"v inst name:   SRLIW\n") }
        is(INST_NAME_SRAW  ) { printf(p"v inst name:   SRAW\n") }
        is(INST_NAME_SRAIW ) { printf(p"v inst name:   SRAIW\n") }
        is(INST_NAME_ADD   ) { printf(p"v inst name:   ADD\n") }
        is(INST_NAME_ADDI  ) { printf(p"v inst name:   ADDI\n") }
        is(INST_NAME_SUB   ) { printf(p"v inst name:   SUB\n") }
        is(INST_NAME_LUI   ) { printf(p"v inst name:   LUI\n") }
        is(INST_NAME_AUIPC ) { printf(p"v inst name:   AUIPC\n") }
        is(INST_NAME_ADDW  ) { printf(p"v inst name:   ADDW\n") }
        is(INST_NAME_ADDIW ) { printf(p"v inst name:   ADDIW\n") }
        is(INST_NAME_SUBW  ) { printf(p"v inst name:   SUBW\n") }
        is(INST_NAME_XORI  ) { printf(p"v inst name:   XORI\n") }
        is(INST_NAME_OR    ) { printf(p"v inst name:   OR\n") }
        is(INST_NAME_AND   ) { printf(p"v inst name:   AND\n") }
        is(INST_NAME_ANDI  ) { printf(p"v inst name:   ANDI\n") }
        is(INST_NAME_SLT   ) { printf(p"v inst name:   SLT\n") }
        is(INST_NAME_SLTU  ) { printf(p"v inst name:   SLTU\n") }
        is(INST_NAME_SLTIU ) { printf(p"v inst name:   SLTIU\n") }
        is(INST_NAME_BEQ   ) { printf(p"v inst name:   BEQ\n") }
        is(INST_NAME_BNE   ) { printf(p"v inst name:   BNE\n") }
        is(INST_NAME_BLT   ) { printf(p"v inst name:   BLT\n") }
        is(INST_NAME_BGE   ) { printf(p"v inst name:   BGE\n") }
        is(INST_NAME_BLTU  ) { printf(p"v inst name:   BLTU\n") }
        is(INST_NAME_BGEU  ) { printf(p"v inst name:   BGEU\n") }
        is(INST_NAME_JAL   ) { printf(p"v inst name:   JAL\n") }
        is(INST_NAME_JALR  ) { printf(p"v inst name:   JALR\n") }
        is(INST_NAME_LB    ) { printf(p"v inst name:   LB\n") }
        is(INST_NAME_LH    ) { printf(p"v inst name:   LH\n") }
        is(INST_NAME_LBU   ) { printf(p"v inst name:   LBU\n") }
        is(INST_NAME_LHU   ) { printf(p"v inst name:   LHU\n") }
        is(INST_NAME_LW    ) { printf(p"v inst name:   LW\n") }
        is(INST_NAME_LD    ) { printf(p"v inst name:   LD\n") }
        is(INST_NAME_SB    ) { printf(p"v inst name:   SB\n") }
        is(INST_NAME_SH    ) { printf(p"v inst name:   SH\n") }
        is(INST_NAME_SW    ) { printf(p"v inst name:   SW\n") }
        is(INST_NAME_SD    ) { printf(p"v inst name:   SD\n") }
        is(INST_NAME_EBREAK) { printf(p"v inst name:   EBREAK\n") }
        is(INST_NAME_MUL   ) { printf(p"v inst name:   MUL\n") }
        is(INST_NAME_MULW  ) { printf(p"v inst name:   MULW\n") }
        is(INST_NAME_DIVU  ) { printf(p"v inst name:   DIVU\n") }
        is(INST_NAME_DIVW  ) { printf(p"v inst name:   DIVW\n") }
        is(INST_NAME_REMU  ) { printf(p"v inst name:   REMU\n") }
        is(INST_NAME_REMW  ) { printf(p"v inst name:   REMW\n") }
    }
    printf("v rs1 addr:   %d\n", idu.io.oInstRS1Addr)
    printf("v rs2 addr:   %d\n", idu.io.oInstRS2Addr)
    printf("v rd  addr:   %d\n", idu.io.oInstRDAddr)
    printf("v rs1 val:     0x%x\n", idu.io.oInstRS1Val)
    printf("v rs2 val:     0x%x\n", idu.io.oInstRS2Val)
    printf("v rd  val:     0x%x\n", exu.io.oRegWrData)
    printf("v mem rd addr: 0x%x\n", exu.io.oMemRdAddr)
    printf("v mem rd data: 0x%x\n", exu.io.iMemRdData)
    printf("v mem wr addr: 0x%x\n", exu.io.oMemWrAddr)
    printf("v mem wr data: 0x%x\n", exu.io.oMemWrData)
    printf("v alu type: %d\n", idu.io.oALUType)
    printf("v alu rs1 val: 0x%x\n", idu.io.oALURS1Val)
    printf("v alu rs2 val: 0x%x\n", idu.io.oALURS2Val)
    printf("v alu out:     0x%x\n", exu.io.oALUOut)
    printf("v jmp en:      %d\n", exu.io.oJmpEn)
    printf("v mem wr en:   %d\n", idu.io.oMemWrEn)
    printf("v reg wr en:   %d\n", idu.io.oRegWrEn)
    printf("v reg(10):     0x%x\n\n", io.oReg)
}
