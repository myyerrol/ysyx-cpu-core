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
    exu.io.iMemRdData   := mem.io.oMemRdData
    exu.io.iInstName    := idu.io.oInstName
    exu.io.iALUType     := idu.io.oALUType
    exu.io.iALURS1Val   := idu.io.oALURS1Val
    exu.io.iALURS2Val   := idu.io.oALURS2Val
    exu.io.iJmpEn       := idu.io.oJmpEn
    exu.io.iMemWrEn     := idu.io.oMemWrEn
    exu.io.iMemByt      := idu.io.oMemByt
    exu.io.iRegWrEn     := idu.io.oRegWrEn
    exu.io.iRegWrSrc    := idu.io.oRegWrSrc

    mem.io.iMemRdAddr   := exu.io.oMemRdAddr

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
    dpi.io.iEbreakFlag := Mux(idu.io.oInstName === INST_NAME_EBREAK, 1.U, 0.U)
    dpi.io.iRegVal := 11.U
    printf("reg addr: %d\n", dpi.io.oRegAddr)
    reg.io.iRegRdEAddr := 10.U(REG_WIDTH.W)
    io.oReg := reg.io.oRegRdEData

    printf("pc:          0x%x\n", io.oPC)
    printf("inst:        0x%x\n", io.iInst)

    val instName = idu.io.oInstName
    switch (instName) {
        is(INST_NAME_SLLI  ) { printf(p"inst name:   SLLI\n") }
        is(INST_NAME_SRLI  ) { printf(p"inst name:   SRLI\n") }
        is(INST_NAME_SRA   ) { printf(p"inst name:   SRA\n") }
        is(INST_NAME_SRAI  ) { printf(p"inst name:   SRAI\n") }
        is(INST_NAME_SLLW  ) { printf(p"inst name:   SLLW\n") }
        is(INST_NAME_SLLIW ) { printf(p"inst name:   SLLIW\n") }
        is(INST_NAME_SRLW  ) { printf(p"inst name:   SRLW\n") }
        is(INST_NAME_SRLIW ) { printf(p"inst name:   SRLIW\n") }
        is(INST_NAME_SRAW  ) { printf(p"inst name:   SRAW\n") }
        is(INST_NAME_SRAIW ) { printf(p"inst name:   SRAIW\n") }
        is(INST_NAME_ADD   ) { printf(p"inst name:   ADD\n") }
        is(INST_NAME_ADDI  ) { printf(p"inst name:   ADDI\n") }
        is(INST_NAME_SUB   ) { printf(p"inst name:   SUB\n") }
        is(INST_NAME_LUI   ) { printf(p"inst name:   LUI\n") }
        is(INST_NAME_AUIPC ) { printf(p"inst name:   AUIPC\n") }
        is(INST_NAME_ADDW  ) { printf(p"inst name:   ADDW\n") }
        is(INST_NAME_ADDIW ) { printf(p"inst name:   ADDIW\n") }
        is(INST_NAME_SUBW  ) { printf(p"inst name:   SUBW\n") }
        is(INST_NAME_XORI  ) { printf(p"inst name:   XORI\n") }
        is(INST_NAME_OR    ) { printf(p"inst name:   OR\n") }
        is(INST_NAME_AND   ) { printf(p"inst name:   AND\n") }
        is(INST_NAME_ANDI  ) { printf(p"inst name:   ANDI\n") }
        is(INST_NAME_SLT   ) { printf(p"inst name:   SLT\n") }
        is(INST_NAME_SLTU  ) { printf(p"inst name:   SLTU\n") }
        is(INST_NAME_SLTIU ) { printf(p"inst name:   SLTIU\n") }
        is(INST_NAME_BEQ   ) { printf(p"inst name:   BEQ\n") }
        is(INST_NAME_BNE   ) { printf(p"inst name:   BNE\n") }
        is(INST_NAME_BLT   ) { printf(p"inst name:   BLT\n") }
        is(INST_NAME_BGE   ) { printf(p"inst name:   BGE\n") }
        is(INST_NAME_BLTU  ) { printf(p"inst name:   BLTU\n") }
        is(INST_NAME_BGEU  ) { printf(p"inst name:   BGEU\n") }
        is(INST_NAME_JAL   ) { printf(p"inst name:   JAL\n") }
        is(INST_NAME_JALR  ) { printf(p"inst name:   JALR\n") }
        is(INST_NAME_LB    ) { printf(p"inst name:   LB\n") }
        is(INST_NAME_LH    ) { printf(p"inst name:   LH\n") }
        is(INST_NAME_LBU   ) { printf(p"inst name:   LBU\n") }
        is(INST_NAME_LHU   ) { printf(p"inst name:   LHU\n") }
        is(INST_NAME_LW    ) { printf(p"inst name:   LW\n") }
        is(INST_NAME_LD    ) { printf(p"inst name:   LD\n") }
        is(INST_NAME_SB    ) { printf(p"inst name:   SB\n") }
        is(INST_NAME_SH    ) { printf(p"inst name:   SH\n") }
        is(INST_NAME_SW    ) { printf(p"inst name:   SW\n") }
        is(INST_NAME_SD    ) { printf(p"inst name:   SD\n") }
        is(INST_NAME_EBREAK) { printf(p"inst name:   EBREAK\n") }
        is(INST_NAME_MUL   ) { printf(p"inst name:   MUL\n") }
        is(INST_NAME_MULW  ) { printf(p"inst name:   MULW\n") }
        is(INST_NAME_DIVU  ) { printf(p"inst name:   DIVU\n") }
        is(INST_NAME_DIVW  ) { printf(p"inst name:   DIVW\n") }
        is(INST_NAME_REMU  ) { printf(p"inst name:   REMU\n") }
        is(INST_NAME_REMW  ) { printf(p"inst name:   REMW\n") }
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
    printf("reg(10):     0x%x\n\n", io.oReg)
}
