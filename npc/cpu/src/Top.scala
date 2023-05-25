import chisel3._
import chisel3.util._

import cpu.comp._
import cpu.dpi._
import cpu.stage._
import cpu.util.Base._
import cpu.util.Inst._

class Top extends Module {
    val io = IO(new Bundle {
        val iItrace = Input(Bool())

        val oPC           = Output(UInt(DATA_WIDTH.W))
        val oRegRdEndData = Output(UInt(DATA_WIDTH.W))
        val oInst         = Output(UInt(DATA_WIDTH.W))
        val oInstCall     = Output(Bool())
        val oInstRet      = Output(Bool())

        val oRegData0  = Output(UInt(DATA_WIDTH.W))
        val oRegData1  = Output(UInt(DATA_WIDTH.W))
        val oRegData2  = Output(UInt(DATA_WIDTH.W))
        val oRegData3  = Output(UInt(DATA_WIDTH.W))
        val oRegData4  = Output(UInt(DATA_WIDTH.W))
        val oRegData5  = Output(UInt(DATA_WIDTH.W))
        val oRegData6  = Output(UInt(DATA_WIDTH.W))
        val oRegData7  = Output(UInt(DATA_WIDTH.W))
        val oRegData8  = Output(UInt(DATA_WIDTH.W))
        val oRegData9  = Output(UInt(DATA_WIDTH.W))
        val oRegData10 = Output(UInt(DATA_WIDTH.W))
        val oRegData11 = Output(UInt(DATA_WIDTH.W))
        val oRegData12 = Output(UInt(DATA_WIDTH.W))
        val oRegData13 = Output(UInt(DATA_WIDTH.W))
        val oRegData14 = Output(UInt(DATA_WIDTH.W))
        val oRegData15 = Output(UInt(DATA_WIDTH.W))
        val oRegData16 = Output(UInt(DATA_WIDTH.W))
        val oRegData17 = Output(UInt(DATA_WIDTH.W))
        val oRegData18 = Output(UInt(DATA_WIDTH.W))
        val oRegData19 = Output(UInt(DATA_WIDTH.W))
        val oRegData20 = Output(UInt(DATA_WIDTH.W))
        val oRegData21 = Output(UInt(DATA_WIDTH.W))
        val oRegData22 = Output(UInt(DATA_WIDTH.W))
        val oRegData23 = Output(UInt(DATA_WIDTH.W))
        val oRegData24 = Output(UInt(DATA_WIDTH.W))
        val oRegData25 = Output(UInt(DATA_WIDTH.W))
        val oRegData26 = Output(UInt(DATA_WIDTH.W))
        val oRegData27 = Output(UInt(DATA_WIDTH.W))
        val oRegData28 = Output(UInt(DATA_WIDTH.W))
        val oRegData29 = Output(UInt(DATA_WIDTH.W))
        val oRegData30 = Output(UInt(DATA_WIDTH.W))
        val oRegData31 = Output(UInt(DATA_WIDTH.W))
    });

    val dpi = Module(new DPI())

    val ifu = Module(new IFU())
    io.oPC := ifu.io.oPC
    dpi.io.iMemRdAddrInst := ifu.io.oPC

    val reg = Module(new Reg())
    val csr = Module(new CSR())

    val idu = Module(new IDU())
    idu.io.iInst       := dpi.io.oMemRdDataInst
    idu.io.iInstRS1Val := reg.io.oRegRd1Data
    idu.io.iInstRS2Val := reg.io.oRegRd2Data
    idu.io.iInstCSRVal := csr.io.oCSRRdData
    idu.io.iPC         := ifu.io.oPC

    reg.io.iRegRd1Addr := idu.io.oInstRS1Addr
    reg.io.iRegRd2Addr := idu.io.oInstRS2Addr
    csr.io.iCSRRdAddr  := idu.io.oInstCSRAddr

    io.oInst := idu.io.iInst
    io.oInstCall := Mux((idu.io.oInstName === INST_NAME_JAL) ||
                       ((idu.io.oInstName === INST_NAME_JALR) &&
                        (idu.io.oInstRDAddr =/= 0.U)), true.B, false.B)
    io.oInstRet  := Mux(((idu.io.oInstName === INST_NAME_JALR) &&
                         (idu.io.oInstRDAddr === 0.U)), true.B, false.B)

    val instName = RegInit(INST_NAME_X)
    instName := idu.io.oInstName

    dpi.io.iEbreakFlag := Mux(instName === INST_NAME_EBREAK, 1.U, 0.U)

    val exu = Module(new EXU())
    exu.io.iInstRS1Addr := idu.io.oInstRS1Addr
    exu.io.iInstRS2Addr := idu.io.oInstRS2Addr
    exu.io.iInstRDAddr  := idu.io.oInstRDAddr
    exu.io.iInstCSRAddr := idu.io.oInstCSRAddr
    exu.io.iInstRS1Val  := idu.io.oInstRS1Val
    exu.io.iInstRS2Val  := idu.io.oInstRS2Val
    exu.io.iInstCSRVal  := idu.io.oInstCSRVal
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

    csr.io.iCSRWrEn       := exu.io.oCSRWrEn
    csr.io.iCSRWrMEn      := exu.io.oCSRWrMEn
    csr.io.iCSRWrAddr     := exu.io.oCSRWrAddr
    csr.io.iCSRWrMEPCAddr := exu.io.oCSRWrMEPCAddr
    csr.io.iCSRWrMCAUAddr := exu.io.oCSRWrMCAUAddr
    csr.io.iCSRWrData     := exu.io.oCSRWrData
    csr.io.iCSRWrMEPCData := exu.io.oCSRWrMEPCData
    csr.io.iCSRWrMCAUData := exu.io.oCSRWrMCAUData

    reg.io.iRegRdEndAddr := 10.U(REG_WIDTH.W)
    io.oRegRdEndData := reg.io.oRegRdEndData

    reg.io.iRegRdSdbAddr := dpi.io.oRegAddr
    dpi.io.iRegData := reg.io.oRegRdSdbData

    reg.io.iRegAddr0  :=  0.U(REG_WIDTH.W)
    reg.io.iRegAddr1  :=  1.U(REG_WIDTH.W)
    reg.io.iRegAddr2  :=  2.U(REG_WIDTH.W)
    reg.io.iRegAddr3  :=  3.U(REG_WIDTH.W)
    reg.io.iRegAddr4  :=  4.U(REG_WIDTH.W)
    reg.io.iRegAddr5  :=  5.U(REG_WIDTH.W)
    reg.io.iRegAddr6  :=  6.U(REG_WIDTH.W)
    reg.io.iRegAddr7  :=  7.U(REG_WIDTH.W)
    reg.io.iRegAddr8  :=  8.U(REG_WIDTH.W)
    reg.io.iRegAddr9  :=  9.U(REG_WIDTH.W)
    reg.io.iRegAddr10 := 10.U(REG_WIDTH.W)
    reg.io.iRegAddr11 := 11.U(REG_WIDTH.W)
    reg.io.iRegAddr12 := 12.U(REG_WIDTH.W)
    reg.io.iRegAddr13 := 13.U(REG_WIDTH.W)
    reg.io.iRegAddr14 := 14.U(REG_WIDTH.W)
    reg.io.iRegAddr15 := 15.U(REG_WIDTH.W)
    reg.io.iRegAddr16 := 16.U(REG_WIDTH.W)
    reg.io.iRegAddr17 := 17.U(REG_WIDTH.W)
    reg.io.iRegAddr18 := 18.U(REG_WIDTH.W)
    reg.io.iRegAddr19 := 19.U(REG_WIDTH.W)
    reg.io.iRegAddr20 := 20.U(REG_WIDTH.W)
    reg.io.iRegAddr21 := 21.U(REG_WIDTH.W)
    reg.io.iRegAddr22 := 22.U(REG_WIDTH.W)
    reg.io.iRegAddr23 := 23.U(REG_WIDTH.W)
    reg.io.iRegAddr24 := 24.U(REG_WIDTH.W)
    reg.io.iRegAddr25 := 25.U(REG_WIDTH.W)
    reg.io.iRegAddr26 := 26.U(REG_WIDTH.W)
    reg.io.iRegAddr27 := 27.U(REG_WIDTH.W)
    reg.io.iRegAddr28 := 28.U(REG_WIDTH.W)
    reg.io.iRegAddr29 := 29.U(REG_WIDTH.W)
    reg.io.iRegAddr30 := 30.U(REG_WIDTH.W)
    reg.io.iRegAddr31 := 31.U(REG_WIDTH.W)

    io.oRegData0  := reg.io.oRegData0
    io.oRegData1  := reg.io.oRegData1
    io.oRegData2  := reg.io.oRegData2
    io.oRegData3  := reg.io.oRegData3
    io.oRegData4  := reg.io.oRegData4
    io.oRegData5  := reg.io.oRegData5
    io.oRegData6  := reg.io.oRegData6
    io.oRegData7  := reg.io.oRegData7
    io.oRegData8  := reg.io.oRegData8
    io.oRegData9  := reg.io.oRegData9
    io.oRegData10 := reg.io.oRegData10
    io.oRegData11 := reg.io.oRegData11
    io.oRegData12 := reg.io.oRegData12
    io.oRegData13 := reg.io.oRegData13
    io.oRegData14 := reg.io.oRegData14
    io.oRegData15 := reg.io.oRegData15
    io.oRegData16 := reg.io.oRegData16
    io.oRegData17 := reg.io.oRegData17
    io.oRegData18 := reg.io.oRegData18
    io.oRegData19 := reg.io.oRegData19
    io.oRegData20 := reg.io.oRegData20
    io.oRegData21 := reg.io.oRegData21
    io.oRegData22 := reg.io.oRegData22
    io.oRegData23 := reg.io.oRegData23
    io.oRegData24 := reg.io.oRegData24
    io.oRegData25 := reg.io.oRegData25
    io.oRegData26 := reg.io.oRegData26
    io.oRegData27 := reg.io.oRegData27
    io.oRegData28 := reg.io.oRegData28
    io.oRegData29 := reg.io.oRegData29
    io.oRegData30 := reg.io.oRegData30
    io.oRegData31 := reg.io.oRegData31

    when (io.iItrace) {
        printf("[itrace] pc:          0x%x\n", ifu.io.oPC)
        printf("[itrace] inst:        0x%x\n", idu.io.iInst)
        val instName = idu.io.oInstName
        switch (instName) {
            is(INST_NAME_SLLI  ) { printf(p"[itrace] inst name:   SLLI\n") }
            is(INST_NAME_SRLI  ) { printf(p"[itrace] inst name:   SRLI\n") }
            is(INST_NAME_SRA   ) { printf(p"[itrace] inst name:   SRA\n") }
            is(INST_NAME_SRAI  ) { printf(p"[itrace] inst name:   SRAI\n") }
            is(INST_NAME_SLLW  ) { printf(p"[itrace] inst name:   SLLW\n") }
            is(INST_NAME_SLLIW ) { printf(p"[itrace] inst name:   SLLIW\n") }
            is(INST_NAME_SRLW  ) { printf(p"[itrace] inst name:   SRLW\n") }
            is(INST_NAME_SRLIW ) { printf(p"[itrace] inst name:   SRLIW\n") }
            is(INST_NAME_SRAW  ) { printf(p"[itrace] inst name:   SRAW\n") }
            is(INST_NAME_SRAIW ) { printf(p"[itrace] inst name:   SRAIW\n") }
            is(INST_NAME_ADD   ) { printf(p"[itrace] inst name:   ADD\n") }
            is(INST_NAME_ADDI  ) { printf(p"[itrace] inst name:   ADDI\n") }
            is(INST_NAME_SUB   ) { printf(p"[itrace] inst name:   SUB\n") }
            is(INST_NAME_LUI   ) { printf(p"[itrace] inst name:   LUI\n") }
            is(INST_NAME_AUIPC ) { printf(p"[itrace] inst name:   AUIPC\n") }
            is(INST_NAME_ADDW  ) { printf(p"[itrace] inst name:   ADDW\n") }
            is(INST_NAME_ADDIW ) { printf(p"[itrace] inst name:   ADDIW\n") }
            is(INST_NAME_SUBW  ) { printf(p"[itrace] inst name:   SUBW\n") }
            is(INST_NAME_XORI  ) { printf(p"[itrace] inst name:   XORI\n") }
            is(INST_NAME_OR    ) { printf(p"[itrace] inst name:   OR\n") }
            is(INST_NAME_AND   ) { printf(p"[itrace] inst name:   AND\n") }
            is(INST_NAME_ANDI  ) { printf(p"[itrace] inst name:   ANDI\n") }
            is(INST_NAME_SLT   ) { printf(p"[itrace] inst name:   SLT\n") }
            is(INST_NAME_SLTU  ) { printf(p"[itrace] inst name:   SLTU\n") }
            is(INST_NAME_SLTIU ) { printf(p"[itrace] inst name:   SLTIU\n") }
            is(INST_NAME_BEQ   ) { printf(p"[itrace] inst name:   BEQ\n") }
            is(INST_NAME_BNE   ) { printf(p"[itrace] inst name:   BNE\n") }
            is(INST_NAME_BLT   ) { printf(p"[itrace] inst name:   BLT\n") }
            is(INST_NAME_BGE   ) { printf(p"[itrace] inst name:   BGE\n") }
            is(INST_NAME_BLTU  ) { printf(p"[itrace] inst name:   BLTU\n") }
            is(INST_NAME_BGEU  ) { printf(p"[itrace] inst name:   BGEU\n") }
            is(INST_NAME_JAL   ) { printf(p"[itrace] inst name:   JAL\n") }
            is(INST_NAME_JALR  ) { printf(p"[itrace] inst name:   JALR\n") }
            is(INST_NAME_LB    ) { printf(p"[itrace] inst name:   LB\n") }
            is(INST_NAME_LH    ) { printf(p"[itrace] inst name:   LH\n") }
            is(INST_NAME_LBU   ) { printf(p"[itrace] inst name:   LBU\n") }
            is(INST_NAME_LHU   ) { printf(p"[itrace] inst name:   LHU\n") }
            is(INST_NAME_LW    ) { printf(p"[itrace] inst name:   LW\n") }
            is(INST_NAME_LD    ) { printf(p"[itrace] inst name:   LD\n") }
            is(INST_NAME_SB    ) { printf(p"[itrace] inst name:   SB\n") }
            is(INST_NAME_SH    ) { printf(p"[itrace] inst name:   SH\n") }
            is(INST_NAME_SW    ) { printf(p"[itrace] inst name:   SW\n") }
            is(INST_NAME_SD    ) { printf(p"[itrace] inst name:   SD\n") }
            is(INST_NAME_EBREAK) { printf(p"[itrace] inst name:   EBREAK\n") }
            is(INST_NAME_MUL   ) { printf(p"[itrace] inst name:   MUL\n") }
            is(INST_NAME_MULW  ) { printf(p"[itrace] inst name:   MULW\n") }
            is(INST_NAME_DIVU  ) { printf(p"[itrace] inst name:   DIVU\n") }
            is(INST_NAME_DIVW  ) { printf(p"[itrace] inst name:   DIVW\n") }
            is(INST_NAME_REMU  ) { printf(p"[itrace] inst name:   REMU\n") }
            is(INST_NAME_REMW  ) { printf(p"[itrace] inst name:   REMW\n") }
        }
        printf("[itrace] rs1 addr:   %d\n", idu.io.oInstRS1Addr)
        printf("[itrace] rs2 addr:   %d\n", idu.io.oInstRS2Addr)
        printf("[itrace] rd  addr:   %d\n", idu.io.oInstRDAddr)
        printf("[itrace] rs1 val:     0x%x\n", idu.io.oInstRS1Val)
        printf("[itrace] rs2 val:     0x%x\n", idu.io.oInstRS2Val)
        printf("[itrace] imm val:     0x%x\n", idu.io.oInstImmVal)
        printf("[itrace] rd  val:     0x%x\n", exu.io.oRegWrData)
        printf("[itrace] mem rd addr: 0x%x\n", exu.io.oMemRdAddr)
        printf("[itrace] mem rd data: 0x%x\n", exu.io.iMemRdData)
        printf("[itrace] mem wr addr: 0x%x\n", exu.io.oMemWrAddr)
        printf("[itrace] mem wr data: 0x%x\n", exu.io.oMemWrData)
        printf("[itrace] alu type: %d\n", idu.io.oALUType)
        printf("[itrace] alu rs1 val: 0x%x\n", idu.io.oALURS1Val)
        printf("[itrace] alu rs2 val: 0x%x\n", idu.io.oALURS2Val)
        printf("[itrace] alu out:     0x%x\n", exu.io.oALUOut)
        printf("[itrace] jmp en:      %d\n", exu.io.oJmpEn)
        printf("[itrace] mem wr en:   %d\n", idu.io.oMemWrEn)
        printf("[itrace] reg wr en:   %d\n", idu.io.oRegWrEn)
        printf("[itrace] reg(10):     0x%x\n\n", io.oRegRdEndData)
    }
}
