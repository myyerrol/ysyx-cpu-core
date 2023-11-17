import chisel3._
import chisel3.util._

import cpu.common._
import cpu.core._
import cpu.dpi._

class Top extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iItrace  = Input(Bool())

        val oPC      = Output(UInt(DATA_WIDTH.W))
        val oInst    = Output(UInt(DATA_WIDTH.W ))
        val oEndData = Output(UInt(DATA_WIDTH.W))
    })

    val mDPI = Module(new DPI())
    val mIFU = Module(new IFU())
    val mIDU = Module(new IDU())
    val mEXU = Module(new EXU())
    val mLSU = Module(new LSU())
    val mWBU = Module(new WBU())

    io.oPC      := mIFU.io.oPC
    io.oInst    := mLSU.io.iMemRdDataInst
    io.oEndData := mIDU.io.oEndData

    mDPI.io.iMemRdAddrInst := mLSU.io.oMemRdAddrInst
    mDPI.io.iMemRdAddrLoad := mLSU.io.oMemRdAddrLoad

    val rInstName = RegNext(mIDU.io.ctrio.oInstName, INST_NAME_X)
    when (rInstName === INST_NAME_X && mIDU.io.ctrio.oStateCurr === 2.U) {
        assert(false.B, "Invalid instruction at 0x%x", mIFU.io.oPC)
    }.elsewhen (rInstName === INST_NAME_EBREAK) {
        mDPI.io.iEbreakFlag := 1.U
    }.otherwise {
        mDPI.io.iEbreakFlag := 0.U
    }

    mIFU.io.iPCWrEn    := mIDU.io.ctrio.oPCWrEn
    mIFU.io.iPCWrConEn := mIDU.io.ctrio.oPCWrConEn
    mIFU.io.iPCWrSrc   := mIDU.io.ctrio.oPCWrSrc
    mIFU.io.iIRWrEn    := mIDU.io.ctrio.oIRWrEn
    mIFU.io.iNPC       := mEXU.io.oNPC
    mIFU.io.iALUZero   := mEXU.io.oALUZero
    mIFU.io.iALUOut    := mEXU.io.oALUOut
    mIFU.io.iInst      := mLSU.io.iMemRdDataInst

    mIDU.io.iPC        := mIFU.io.oPC
    mIDU.io.iInst      := mIFU.io.oInst
    mIDU.io.iGPRWrData := mWBU.io.oGPRWrData

    mEXU.io.iPCNextEn := mIDU.io.ctrio.oPCNextEn
    mEXU.io.iALUType  := mIDU.io.ctrio.oALUType
    mEXU.io.iALURS1   := mIDU.io.ctrio.oALURS1
    mEXU.io.iALURS2   := mIDU.io.ctrio.oALURS2
    mEXU.io.iPC       := mIFU.io.oPC
    mEXU.io.iRS1Data  := mIDU.io.oRS1Data
    mEXU.io.iRS2Data  := mIDU.io.oRS2Data
    mEXU.io.iImmData  := mIDU.io.oImmData

    mLSU.io.iPC        := mIFU.io.oPC
    mLSU.io.iALUOut    := mEXU.io.oALUOut
    mLSU.io.iMemWrEn   := mIDU.io.ctrio.oMemWrEn
    mLSU.io.iMemByt    := mIDU.io.ctrio.oMemByt
    mLSU.io.iMemWrData := mEXU.io.oMemWrData

    mLSU.io.iMemRdDataInst := mDPI.io.oMemRdDataInst
    mLSU.io.iMemRdDataLoad := mDPI.io.oMemRdDataLoad

    mWBU.io.iMemByt   := mIDU.io.ctrio.oMemByt
    mWBU.io.iGPRWrSrc := mIDU.io.ctrio.oGPRWrSrc
    mWBU.io.iALUOut   := mEXU.io.oALUOut
    mWBU.io.iMemData  := mLSU.io.oMemRdData

    when (io.iItrace) {
        printf("[itrace] [ifu]     pc:             0x%x\n", mIFU.io.oPC)
        printf("[itrace] [ifu]     inst:           0x%x\n", mIFU.io.oInst)
        switch (mIDU.io.ctrio.oInstName) {
            is (INST_NAME_SLLI  ) { printf(p"[itrace] [idu ctr] inst name:      SLLI\n") }
            is (INST_NAME_SRLI  ) { printf(p"[itrace] [idu ctr] inst name:      SRLI\n") }
            is (INST_NAME_SRA   ) { printf(p"[itrace] [idu ctr] inst name:      SRA\n") }
            is (INST_NAME_SRAI  ) { printf(p"[itrace] [idu ctr] inst name:      SRAI\n") }
            is (INST_NAME_SLLW  ) { printf(p"[itrace] [idu ctr] inst name:      SLLW\n") }
            is (INST_NAME_SLLIW ) { printf(p"[itrace] [idu ctr] inst name:      SLLIW\n") }
            is (INST_NAME_SRLW  ) { printf(p"[itrace] [idu ctr] inst name:      SRLW\n") }
            is (INST_NAME_SRLIW ) { printf(p"[itrace] [idu ctr] inst name:      SRLIW\n") }
            is (INST_NAME_SRAW  ) { printf(p"[itrace] [idu ctr] inst name:      SRAW\n") }
            is (INST_NAME_SRAIW ) { printf(p"[itrace] [idu ctr] inst name:      SRAIW\n") }
            is (INST_NAME_ADD   ) { printf(p"[itrace] [idu ctr] inst name:      ADD\n") }
            is (INST_NAME_ADDI  ) { printf(p"[itrace] [idu ctr] inst name:      ADDI\n") }
            is (INST_NAME_SUB   ) { printf(p"[itrace] [idu ctr] inst name:      SUB\n") }
            is (INST_NAME_LUI   ) { printf(p"[itrace] [idu ctr] inst name:      LUI\n") }
            is (INST_NAME_AUIPC ) { printf(p"[itrace] [idu ctr] inst name:      AUIPC\n") }
            is (INST_NAME_ADDW  ) { printf(p"[itrace] [idu ctr] inst name:      ADDW\n") }
            is (INST_NAME_ADDIW ) { printf(p"[itrace] [idu ctr] inst name:      ADDIW\n") }
            is (INST_NAME_SUBW  ) { printf(p"[itrace] [idu ctr] inst name:      SUBW\n") }
            is (INST_NAME_XORI  ) { printf(p"[itrace] [idu ctr] inst name:      XORI\n") }
            is (INST_NAME_OR    ) { printf(p"[itrace] [idu ctr] inst name:      OR\n") }
            is (INST_NAME_AND   ) { printf(p"[itrace] [idu ctr] inst name:      AND\n") }
            is (INST_NAME_ANDI  ) { printf(p"[itrace] [idu ctr] inst name:      ANDI\n") }
            is (INST_NAME_SLT   ) { printf(p"[itrace] [idu ctr] inst name:      SLT\n") }
            is (INST_NAME_SLTU  ) { printf(p"[itrace] [idu ctr] inst name:      SLTU\n") }
            is (INST_NAME_SLTIU ) { printf(p"[itrace] [idu ctr] inst name:      SLTIU\n") }
            is (INST_NAME_BEQ   ) { printf(p"[itrace] [idu ctr] inst name:      BEQ\n") }
            is (INST_NAME_BNE   ) { printf(p"[itrace] [idu ctr] inst name:      BNE\n") }
            is (INST_NAME_BLT   ) { printf(p"[itrace] [idu ctr] inst name:      BLT\n") }
            is (INST_NAME_BGE   ) { printf(p"[itrace] [idu ctr] inst name:      BGE\n") }
            is (INST_NAME_BLTU  ) { printf(p"[itrace] [idu ctr] inst name:      BLTU\n") }
            is (INST_NAME_BGEU  ) { printf(p"[itrace] [idu ctr] inst name:      BGEU\n") }
            is (INST_NAME_JAL   ) { printf(p"[itrace] [idu ctr] inst name:      JAL\n") }
            is (INST_NAME_JALR  ) { printf(p"[itrace] [idu ctr] inst name:      JALR\n") }
            is (INST_NAME_LB    ) { printf(p"[itrace] [idu ctr] inst name:      LB\n") }
            is (INST_NAME_LH    ) { printf(p"[itrace] [idu ctr] inst name:      LH\n") }
            is (INST_NAME_LBU   ) { printf(p"[itrace] [idu ctr] inst name:      LBU\n") }
            is (INST_NAME_LHU   ) { printf(p"[itrace] [idu ctr] inst name:      LHU\n") }
            is (INST_NAME_LW    ) { printf(p"[itrace] [idu ctr] inst name:      LW\n") }
            is (INST_NAME_LD    ) { printf(p"[itrace] [idu ctr] inst name:      LD\n") }
            is (INST_NAME_SB    ) { printf(p"[itrace] [idu ctr] inst name:      SB\n") }
            is (INST_NAME_SH    ) { printf(p"[itrace] [idu ctr] inst name:      SH\n") }
            is (INST_NAME_SW    ) { printf(p"[itrace] [idu ctr] inst name:      SW\n") }
            is (INST_NAME_SD    ) { printf(p"[itrace] [idu ctr] inst name:      SD\n") }
            is (INST_NAME_ECALL ) { printf(p"[itrace] [idu ctr] inst name:      ECALL\n") }
            is (INST_NAME_EBREAK) { printf(p"[itrace] [idu ctr] inst name:      EBREAK\n") }
            is (INST_NAME_CSRRW ) { printf(p"[itrace] [idu ctr] inst name:      CSRRW\n") }
            is (INST_NAME_CSRRS ) { printf(p"[itrace] [idu ctr] inst name:      CSRRS\n") }
            is (INST_NAME_MRET  ) { printf(p"[itrace] [idu ctr] inst name:      MRET\n") }
            is (INST_NAME_MUL   ) { printf(p"[itrace] [idu ctr] inst name:      MUL\n") }
            is (INST_NAME_MULW  ) { printf(p"[itrace] [idu ctr] inst name:      MULW\n") }
            is (INST_NAME_DIVU  ) { printf(p"[itrace] [idu ctr] inst name:      DIVU\n") }
            is (INST_NAME_DIVW  ) { printf(p"[itrace] [idu ctr] inst name:      DIVW\n") }
            is (INST_NAME_REMU  ) { printf(p"[itrace] [idu ctr] inst name:      REMU\n") }
            is (INST_NAME_REMW  ) { printf(p"[itrace] [idu ctr] inst name:      REMW\n") }
        }
        switch (mIDU.io.ctrio.oStateCurr) {
            is (0.U) { printf(p"[itrace] [idu ctr] state curr:     STATE_IF\n")}
            is (1.U) { printf(p"[itrace] [idu ctr] state curr:     STATE_ID\n")}
            is (2.U) { printf(p"[itrace] [idu ctr] state curr:     STATE_EX\n")}
            is (3.U) { printf(p"[itrace] [idu ctr] state curr:     STATE_LS\n")}
            is (4.U) { printf(p"[itrace] [idu ctr] state curr:     STATE_WB\n")}
        }
        printf("[itrace] [idu ctr] pc wr en:       %d\n", mIDU.io.ctrio.oPCWrEn)
        printf("[itrace] [idu ctr] pc wr con en:   %d\n", mIDU.io.ctrio.oPCWrConEn)
        printf("[itrace] [idu ctr] pc wr src:   %d\n", mIDU.io.ctrio.oPCWrSrc)
        printf("[itrace] [idu ctr] pc next en:     %d\n", mIDU.io.ctrio.oPCNextEn)
        printf("[itrace] [idu ctr] mem wr en:      %d\n", mIDU.io.ctrio.oMemWrEn)
        printf("[itrace] [idu ctr] mem byt:     %d\n", mIDU.io.ctrio.oMemByt)
        printf("[itrace] [idu ctr] ir wr en:       %d\n", mIDU.io.ctrio.oIRWrEn)
        printf("[itrace] [idu ctr] gpr wr en:      %d\n", mIDU.io.ctrio.oGPRWrEn)
        printf("[itrace] [idu ctr] gpr wr src:  %d\n", mIDU.io.ctrio.oGPRWrSrc)
        printf("[itrace] [idu ctr] alu type:    %d\n", mIDU.io.ctrio.oALUType)
        printf("[itrace] [idu ctr] alu rs1:     %d\n", mIDU.io.ctrio.oALURS1)
        printf("[itrace] [idu ctr] alu rs2:     %d\n", mIDU.io.ctrio.oALURS2)

        printf("[itrace] [idu]     rs1 addr:     %d\n", mIDU.io.oRS1Addr)
        printf("[itrace] [idu]     rs2 addr:     %d\n", mIDU.io.oRS2Addr)
        printf("[itrace] [idu]     rd  addr:     %d\n", mIDU.io.oRDAddr)
        printf("[itrace] [idu]     rs1 data:       0x%x\n", mIDU.io.oRS1Data)
        printf("[itrace] [idu]     rs2 data:       0x%x\n", mIDU.io.oRS2Data)
        printf("[itrace] [idu]     end data:       0x%x\n", mIDU.io.oEndData)
        printf("[itrace] [idu]     imm data:       0x%x\n", mIDU.io.oImmData)

        printf("[itrace] [exu]     npc:            0x%x\n", mEXU.io.oNPC)
        printf("[itrace] [exu]     alu zero:       %d\n", mEXU.io.oALUZero)
        printf("[itrace] [exu]     alu out:        0x%x\n", mEXU.io.oALUOut)

        printf("\n")
    }
}
