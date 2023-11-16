import chisel3._
import chisel3.util._

import cpu.common._
import cpu.core._
import cpu.dpi._

class Top extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iItrace = Input(Bool())

        val oPC           = Output(UInt(DATA_WIDTH.W))
        val oInst         = Output(UInt(DATA_WIDTH.W))
        val oRegRdEndData = Output(UInt(DATA_WIDTH.W))
    })

    val mDPI = Module(new DPI())
    val mIFU = Module(new IFU())
    val mIDU = Module(new IDU())
    val mEXU = Module(new EXU())
    val mWBU = Module(new WBU())

    io.oPC   := mIFU.io.oPC
    io.oInst := mDPI.io.oMemRdDataInst
    io.oRegRdEndData := mIDU.io.oRdEndData

    mDPI.io.iMemRdAddrInst := mIFU.io.oPC
    // 延迟一个时钟周期是为了能够打印出EBREAK指令
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
    mIFU.io.iZero      := mEXU.io.oZero
    mIFU.io.iInst      := mDPI.io.oMemRdDataInst
    mIFU.io.iImm       := 0.U
    mIFU.io.iNPC       := mEXU.io.oNPC
    mIFU.io.iALUOut    := mEXU.io.oALUOut

    mIDU.io.iPC     := mIFU.io.oPC
    mIDU.io.iInst   := mIFU.io.oInst
    mIDU.io.iWrData := mWBU.io.oWrData

    mEXU.io.iALUType := mIDU.io.ctrio.oALUType
    mEXU.io.iALURS1  := mIDU.io.ctrio.oALURS1
    mEXU.io.iALURS2  := mIDU.io.ctrio.oALURS2
    mEXU.io.iPC      := mIFU.io.oPC
    mEXU.io.iRS1Data := mIDU.io.oRS1Data
    mEXU.io.iRS2Data := mIDU.io.oRS2Data
    mEXU.io.iImmData := mIDU.io.oImmData

    mWBU.io.iWrSrc   := mIDU.io.ctrio.oGPRWrSrc
    mWBU.io.iALUOut  := mEXU.io.oALUOut
    mWBU.io.iMemData := 0.U

    when (io.iItrace) {
        printf("[itrace] pc:          0x%x\n", mIFU.io.oPC)
        printf("[itrace] inst:        0x%x\n", mDPI.io.oMemRdDataInst)
        switch (mIDU.io.ctrio.oInstName) {
            is (INST_NAME_SLLI  ) { printf(p"[itrace] inst name:   SLLI\n") }
            is (INST_NAME_SRLI  ) { printf(p"[itrace] inst name:   SRLI\n") }
            is (INST_NAME_SRA   ) { printf(p"[itrace] inst name:   SRA\n") }
            is (INST_NAME_SRAI  ) { printf(p"[itrace] inst name:   SRAI\n") }
            is (INST_NAME_SLLW  ) { printf(p"[itrace] inst name:   SLLW\n") }
            is (INST_NAME_SLLIW ) { printf(p"[itrace] inst name:   SLLIW\n") }
            is (INST_NAME_SRLW  ) { printf(p"[itrace] inst name:   SRLW\n") }
            is (INST_NAME_SRLIW ) { printf(p"[itrace] inst name:   SRLIW\n") }
            is (INST_NAME_SRAW  ) { printf(p"[itrace] inst name:   SRAW\n") }
            is (INST_NAME_SRAIW ) { printf(p"[itrace] inst name:   SRAIW\n") }
            is (INST_NAME_ADD   ) { printf(p"[itrace] inst name:   ADD\n") }
            is (INST_NAME_ADDI  ) { printf(p"[itrace] inst name:   ADDI\n") }
            is (INST_NAME_SUB   ) { printf(p"[itrace] inst name:   SUB\n") }
            is (INST_NAME_LUI   ) { printf(p"[itrace] inst name:   LUI\n") }
            is (INST_NAME_AUIPC ) { printf(p"[itrace] inst name:   AUIPC\n") }
            is (INST_NAME_ADDW  ) { printf(p"[itrace] inst name:   ADDW\n") }
            is (INST_NAME_ADDIW ) { printf(p"[itrace] inst name:   ADDIW\n") }
            is (INST_NAME_SUBW  ) { printf(p"[itrace] inst name:   SUBW\n") }
            is (INST_NAME_XORI  ) { printf(p"[itrace] inst name:   XORI\n") }
            is (INST_NAME_OR    ) { printf(p"[itrace] inst name:   OR\n") }
            is (INST_NAME_AND   ) { printf(p"[itrace] inst name:   AND\n") }
            is (INST_NAME_ANDI  ) { printf(p"[itrace] inst name:   ANDI\n") }
            is (INST_NAME_SLT   ) { printf(p"[itrace] inst name:   SLT\n") }
            is (INST_NAME_SLTU  ) { printf(p"[itrace] inst name:   SLTU\n") }
            is (INST_NAME_SLTIU ) { printf(p"[itrace] inst name:   SLTIU\n") }
            is (INST_NAME_BEQ   ) { printf(p"[itrace] inst name:   BEQ\n") }
            is (INST_NAME_BNE   ) { printf(p"[itrace] inst name:   BNE\n") }
            is (INST_NAME_BLT   ) { printf(p"[itrace] inst name:   BLT\n") }
            is (INST_NAME_BGE   ) { printf(p"[itrace] inst name:   BGE\n") }
            is (INST_NAME_BLTU  ) { printf(p"[itrace] inst name:   BLTU\n") }
            is (INST_NAME_BGEU  ) { printf(p"[itrace] inst name:   BGEU\n") }
            is (INST_NAME_JAL   ) { printf(p"[itrace] inst name:   JAL\n") }
            is (INST_NAME_JALR  ) { printf(p"[itrace] inst name:   JALR\n") }
            is (INST_NAME_LB    ) { printf(p"[itrace] inst name:   LB\n") }
            is (INST_NAME_LH    ) { printf(p"[itrace] inst name:   LH\n") }
            is (INST_NAME_LBU   ) { printf(p"[itrace] inst name:   LBU\n") }
            is (INST_NAME_LHU   ) { printf(p"[itrace] inst name:   LHU\n") }
            is (INST_NAME_LW    ) { printf(p"[itrace] inst name:   LW\n") }
            is (INST_NAME_LD    ) { printf(p"[itrace] inst name:   LD\n") }
            is (INST_NAME_SB    ) { printf(p"[itrace] inst name:   SB\n") }
            is (INST_NAME_SH    ) { printf(p"[itrace] inst name:   SH\n") }
            is (INST_NAME_SW    ) { printf(p"[itrace] inst name:   SW\n") }
            is (INST_NAME_SD    ) { printf(p"[itrace] inst name:   SD\n") }
            is (INST_NAME_ECALL ) { printf(p"[itrace] inst name:   ECALL\n") }
            is (INST_NAME_EBREAK) { printf(p"[itrace] inst name:   EBREAK\n") }
            is (INST_NAME_CSRRW ) { printf(p"[itrace] inst name:   CSRRW\n") }
            is (INST_NAME_CSRRS ) { printf(p"[itrace] inst name:   CSRRS\n") }
            is (INST_NAME_MRET  ) { printf(p"[itrace] inst name:   MRET\n") }
            is (INST_NAME_MUL   ) { printf(p"[itrace] inst name:   MUL\n") }
            is (INST_NAME_MULW  ) { printf(p"[itrace] inst name:   MULW\n") }
            is (INST_NAME_DIVU  ) { printf(p"[itrace] inst name:   DIVU\n") }
            is (INST_NAME_DIVW  ) { printf(p"[itrace] inst name:   DIVW\n") }
            is (INST_NAME_REMU  ) { printf(p"[itrace] inst name:   REMU\n") }
            is (INST_NAME_REMW  ) { printf(p"[itrace] inst name:   REMW\n") }
        }

        switch (mIDU.io.ctrio.oStateCurr) {
            is (0.U) { printf(p"[itrace] state: STATE_IF\n")}
            is (1.U) { printf(p"[itrace] state: STATE_ID\n")}
            is (2.U) { printf(p"[itrace] state: STATE_EX\n")}
            is (3.U) { printf(p"[itrace] state: STATE_LS\n")}
            is (4.U) { printf(p"[itrace] state: STATE_WB\n")}
        }

        printf("[itrace] oPCWrEn    : %d\n", mIDU.io.ctrio.oPCWrEn    )
        printf("[itrace] oPCWrConEn : %d\n", mIDU.io.ctrio.oPCWrConEn )
        printf("[itrace] oPCWrSrc   : %d\n", mIDU.io.ctrio.oPCWrSrc   )
        printf("[itrace] oMemWrEn   : %d\n", mIDU.io.ctrio.oMemWrEn   )
        printf("[itrace] oMemRdEn   : %d\n", mIDU.io.ctrio.oMemRdEn   )
        printf("[itrace] oMemByt    : %d\n", mIDU.io.ctrio.oMemByt    )
        printf("[itrace] oMemAddrSrc: %d\n", mIDU.io.ctrio.oMemAddrSrc)
        printf("[itrace] oIRWrEn    : %d\n", mIDU.io.ctrio.oIRWrEn    )
        printf("[itrace] oGPRWrEn   : %d\n", mIDU.io.ctrio.oGPRWrEn   )
        printf("[itrace] oGPRWrSrc  : %d\n", mIDU.io.ctrio.oGPRWrSrc  )
        printf("[itrace] oALUType   : %d\n", mIDU.io.ctrio.oALUType   )
        printf("[itrace] oALURS1    : %d\n", mIDU.io.ctrio.oALURS1    )
        printf("[itrace] oALURS2    : %d\n", mIDU.io.ctrio.oALURS2    )






        printf("[itrace] rs1 addr:   %d\n", mIDU.io.oRS1Addr)
        printf("[itrace] rs2 addr:   %d\n", mIDU.io.oRS2Addr)
        printf("[itrace] rd  addr:   %d\n", mIDU.io.oRDAddr)
        printf("[itrace] rs1 val:     0x%x\n", mIDU.io.oRS1Data)
        printf("[itrace] rs2 val:     0x%x\n", mIDU.io.oRS2Data)
        printf("[itrace] imm val:     0x%x\n", mIDU.io.oImmData)
        printf("[itrace] rd  val:     0x%x\n", mWBU.io.oWrData)


        printf("[itrace] iZero:     0x%x\n", mIFU.io.iZero)
        printf("[itrace] iNPC:     0x%x\n", mIFU.io.iNPC)
        printf("[itrace] iALUOut:     0x%x\n", mIFU.io.iALUOut)


        printf("\n")

        // printf("[itrace] mem rd addr: 0x%x\n", exu.io.oMemRdAddr)
        // printf("[itrace] mem rd data: 0x%x\n", exu.io.iMemRdData)
        // printf("[itrace] mem wr addr: 0x%x\n", exu.io.oMemWrAddr)
        // printf("[itrace] mem wr data: 0x%x\n", exu.io.oMemWrData)
        // printf("[itrace] alu type: %d\n", idu.io.oALUType)
        // printf("[itrace] alu rs1 val: 0x%x\n", idu.io.oALURS1Val)
        // printf("[itrace] alu rs2 val: 0x%x\n", idu.io.oALURS2Val)
        // printf("[itrace] alu out:     0x%x\n", exu.io.oALUOut)
        // printf("[itrace] jmp en:      %d\n", exu.io.oJmpEn)
        // printf("[itrace] mem wr en:   %d\n", idu.io.oMemWrEn)
        // printf("[itrace] reg wr en:   %d\n", idu.io.oRegWrEn)
        // printf("[itrace] reg(10):     0x%x\n\n", io.oRegRdEndData)
    }
}
