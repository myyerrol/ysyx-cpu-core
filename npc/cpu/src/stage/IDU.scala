package cpu.stage

import chisel3._
import chisel3.util._

import cpu.util.Base._
import cpu.util.Inst._

class IDU extends Module {
    val io = IO(new Bundle {
        val iInst        = Input(UInt(DATA_WIDTH.W))
        val iInstRS1Val  = Input(UInt(DATA_WIDTH.W))
        val iInstRS2Val  = Input(UInt(DATA_WIDTH.W))
        val iInstCSRVal  = Input(UInt(DATA_WIDTH.W))
        val iPC          = Input(UInt(DATA_WIDTH.W))

        val oInstRS1Addr = Output(UInt(REG_WIDTH.W))
        val oInstRS2Addr = Output(UInt(REG_WIDTH.W))
        val oInstRDAddr  = Output(UInt(REG_WIDTH.W))
        val oInstCSRAddr = Output(UInt(DATA_WIDTH.W))
        val oInstRS1Val  = Output(UInt(DATA_WIDTH.W))
        val oInstRS2Val  = Output(UInt(DATA_WIDTH.W))
        val oInstCSRVal  = Output(UInt(DATA_WIDTH.W))
        val oInstImmVal  = Output(UInt(DATA_WIDTH.W))

        val oInstName    = Output(UInt(SIGNAL_WIDTH.W))
        val oALUType     = Output(UInt(SIGNAL_WIDTH.W))
        val oALURS1Val   = Output(UInt(DATA_WIDTH.W))
        val oALURS2Val   = Output(UInt(DATA_WIDTH.W))
        val oJmpEn       = Output(Bool())
        val oMemWrEn     = Output(Bool())
        val oMemByt      = Output(UInt(SIGNAL_WIDTH.W))
        val oRegWrEn     = Output(Bool())
        val oRegWrSrc    = Output(UInt(SIGNAL_WIDTH.W))
    })

    val inst = io.iInst;
    var signals = ListLookup(
        inst,
        List(INST_NAME_X, ALU_TYPE_X, ALU_RS1_X, ALU_RS2_X, JMP_F, MEM_WR_F, MEM_BYT_X, REG_WR_F, REG_WR_SRC_X),
        Array(
            SLL    -> List(INST_NAME_SLL,    ALU_TYPE_SLL,   ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            SLLI   -> List(INST_NAME_SLLI,   ALU_TYPE_SLL,   ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            SRLI   -> List(INST_NAME_SRLI,   ALU_TYPE_SRL,   ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            SRA    -> List(INST_NAME_SRA,    ALU_TYPE_SRA,   ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            SRAI   -> List(INST_NAME_SRAI,   ALU_TYPE_SRA,   ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            SLLW   -> List(INST_NAME_SLLW,   ALU_TYPE_SLLW,  ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            SLLIW  -> List(INST_NAME_SLLIW,  ALU_TYPE_SLL,   ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            SRLW   -> List(INST_NAME_SRLW,   ALU_TYPE_SRLW,  ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            SRLIW  -> List(INST_NAME_SRLIW,  ALU_TYPE_SRLIW, ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            SRAW   -> List(INST_NAME_SRAW,   ALU_TYPE_SRAW,  ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            SRAIW  -> List(INST_NAME_SRAIW,  ALU_TYPE_SRAIW, ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),

            ADD    -> List(INST_NAME_ADD,    ALU_TYPE_ADD,   ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            ADDI   -> List(INST_NAME_ADDI,   ALU_TYPE_ADD,   ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            SUB    -> List(INST_NAME_SUB,    ALU_TYPE_SUB,   ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            LUI    -> List(INST_NAME_LUI,    ALU_TYPE_ADD,   ALU_RS1_X,  ALU_RS2_IMM_U, JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            AUIPC  -> List(INST_NAME_AUIPC,  ALU_TYPE_ADD,   ALU_RS1_PC, ALU_RS2_IMM_U, JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            ADDW   -> List(INST_NAME_ADDW,   ALU_TYPE_ADD,   ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            ADDIW  -> List(INST_NAME_ADDIW,  ALU_TYPE_ADD,   ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            SUBW   -> List(INST_NAME_SUBW,   ALU_TYPE_SUB,   ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),

            XOR    -> List(INST_NAME_XOR,    ALU_TYPE_XOR,   ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            XORI   -> List(INST_NAME_XORI,   ALU_TYPE_XOR,   ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            OR     -> List(INST_NAME_OR,     ALU_TYPE_OR,    ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            ORI    -> List(INST_NAME_ORI,    ALU_TYPE_OR,    ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            AND    -> List(INST_NAME_AND,    ALU_TYPE_AND,   ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            ANDI   -> List(INST_NAME_ANDI,   ALU_TYPE_AND,   ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),

            SLT    -> List(INST_NAME_SLT,    ALU_TYPE_SLT,   ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            SLTU   -> List(INST_NAME_SLTU,   ALU_TYPE_SLTU,  ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            SLTIU  -> List(INST_NAME_SLTIU,  ALU_TYPE_SLTU,  ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),

            BEQ    -> List(INST_NAME_BEQ,    ALU_TYPE_BEQ,   ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_F, REG_WR_SRC_X),
            BNE    -> List(INST_NAME_BNE,    ALU_TYPE_BNE,   ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_F, REG_WR_SRC_X),
            BLT    -> List(INST_NAME_BLT,    ALU_TYPE_BLT,   ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_F, REG_WR_SRC_X),
            BGE    -> List(INST_NAME_BGE,    ALU_TYPE_BGE,   ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_F, REG_WR_SRC_X),
            BLTU   -> List(INST_NAME_BLTU,   ALU_TYPE_BLTU,  ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_F, REG_WR_SRC_X),
            BGEU   -> List(INST_NAME_BGEU,   ALU_TYPE_BGEU,  ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_F, REG_WR_SRC_X),

            JAL    -> List(INST_NAME_JAL,    ALU_TYPE_ADD,   ALU_RS1_PC, ALU_RS2_IMM_J, JMP_T, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_PC),
            JALR   -> List(INST_NAME_JALR,   ALU_TYPE_JALR,  ALU_RS1_R,  ALU_RS2_IMM_I, JMP_T, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_PC),

            LB     -> List(INST_NAME_LB,     ALU_TYPE_ADD,   ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_1_S, REG_WR_T, REG_WR_SRC_MEM),
            LH     -> List(INST_NAME_LH,     ALU_TYPE_ADD,   ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_2_S, REG_WR_T, REG_WR_SRC_MEM),
            LBU    -> List(INST_NAME_LBU,    ALU_TYPE_ADD,   ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_1_U, REG_WR_T, REG_WR_SRC_MEM),
            LHU    -> List(INST_NAME_LHU,    ALU_TYPE_ADD,   ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_2_U, REG_WR_T, REG_WR_SRC_MEM),
            LW     -> List(INST_NAME_LW,     ALU_TYPE_ADD,   ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_4_S, REG_WR_T, REG_WR_SRC_MEM),
            LWU    -> List(INST_NAME_LWU,    ALU_TYPE_ADD,   ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_4_U, REG_WR_T, REG_WR_SRC_MEM),
            LD     -> List(INST_NAME_LD,     ALU_TYPE_ADD,   ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, MEM_BYT_8_S, REG_WR_T, REG_WR_SRC_MEM),

            SB     -> List(INST_NAME_SB,     ALU_TYPE_ADD,   ALU_RS1_R,  ALU_RS2_IMM_S, JMP_F, MEM_WR_T, MEM_BYT_1_U, REG_WR_F, REG_WR_SRC_X),
            SH     -> List(INST_NAME_SH,     ALU_TYPE_ADD,   ALU_RS1_R,  ALU_RS2_IMM_S, JMP_F, MEM_WR_T, MEM_BYT_2_U, REG_WR_F, REG_WR_SRC_X),
            SW     -> List(INST_NAME_SW,     ALU_TYPE_ADD,   ALU_RS1_R,  ALU_RS2_IMM_S, JMP_F, MEM_WR_T, MEM_BYT_4_U, REG_WR_F, REG_WR_SRC_X),
            SD     -> List(INST_NAME_SD,     ALU_TYPE_ADD,   ALU_RS1_R,  ALU_RS2_IMM_S, JMP_F, MEM_WR_T, MEM_BYT_8_U, REG_WR_F, REG_WR_SRC_X),

            ECALL  -> List(INST_NAME_ECALL,  ALU_TYPE_X,     ALU_RS1_X,  ALU_RS2_X,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_F, REG_WR_SRC_X),
            EBREAK -> List(INST_NAME_EBREAK, ALU_TYPE_X,     ALU_RS1_X,  ALU_RS2_X,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_F, REG_WR_SRC_X),

            CSRRW  -> List(INST_NAME_CSRRW,  ALU_TYPE_OR,    ALU_RS1_R,  ALU_RS2_CSR,   JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_CSR),
            CSRRS  -> List(INST_NAME_CSRRS,  ALU_TYPE_OR,    ALU_RS1_R,  ALU_RS2_CSR,   JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_CSR),
            MRET   -> List(INST_NAME_MRET,   ALU_TYPE_ADD,   ALU_RS1_X,  ALU_RS2_CSR,   JMP_T, MEM_WR_F, MEM_BYT_X,   REG_WR_F, REG_WR_SRC_X),

            MUL    -> List(INST_NAME_MUL,    ALU_TYPE_MUL,   ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            MULW   -> List(INST_NAME_MULW,   ALU_TYPE_MUL,   ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            DIVU   -> List(INST_NAME_DIVU,   ALU_TYPE_DIVU,  ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            DIVW   -> List(INST_NAME_DIVW,   ALU_TYPE_DIVW,  ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            DIVUW  -> List(INST_NAME_DIVUW,  ALU_TYPE_DIVUW, ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            REMU   -> List(INST_NAME_REMU,   ALU_TYPE_REMU,  ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU),
            REMW   -> List(INST_NAME_REMW,   ALU_TYPE_REMW,  ALU_RS1_R,  ALU_RS2_R,     JMP_F, MEM_WR_F, MEM_BYT_X,   REG_WR_T, REG_WR_SRC_ALU))
    )
    val instName = signals(0)
    val aluType  = signals(1)
    val aluRS1   = signals(2)
    val aluRS2   = signals(3)
    val jmpEn    = signals(4)
    val memWrEn  = signals(5)
    val memByt   = signals(6)
    val regWrEn  = signals(7)
    val regWrSrc = signals(8)

    when (instName === INST_NAME_X) {
        assert(false.B, "Invalid instruction at 0x%x", io.iPC)
    }

    val instRS1Addr  = inst(19, 15)
    val instRS2Addr  = inst(24, 20)
    val instRDAddr   = inst(11, 7)
    val instImmI     = inst(31, 20)
    val instImmISext = Cat(Fill(52, instImmI(11)), instImmI)
    val instImmS     = Cat(inst(31, 25), inst(11, 7))
    val instImmSSext = Cat(Fill(52, instImmS(11)), instImmS)
    val instImmB     = Cat(inst(31), inst(7), inst(30, 25), inst(11, 8))
    val instImmBSext = Cat(Fill(51, instImmB(11)), instImmB, 0.U(1.U))
    val instImmU     = inst(31, 12)
    val instImmUSext = Cat(Fill(32, instImmU(19)), instImmU, Fill(12, 0.U))
    val instImmJ     = Cat(inst(31), inst(19, 12), inst(20), inst(30, 21))
    val instImmJSext = Cat(Fill(43, instImmJ(19)), instImmJ, 0.U(1.U))

    io.oInstRS1Addr := instRS1Addr
    io.oInstRS2Addr := instRS2Addr
    io.oInstCSRAddr:= MuxCase(
        0.U(DATA_WIDTH.W),
        Seq(
            ((instName === INST_NAME_CSRRW) ||
             (instName === INST_NAME_CSRRS)) -> instImmISext,
             (instName === INST_NAME_MRET)   -> CSR_MEPC
        )
    )

    val aluRS1Val = MuxCase(
        0.U(DATA_WIDTH.W),
        Seq(
            (aluRS1 === ALU_RS1_X)   -> 0.U(DATA_WIDTH.W),
            (aluRS1 === ALU_RS1_R)   -> io.iInstRS1Val,
            (aluRS1 === ALU_RS1_PC)  -> io.iPC
        )
    )
    val aluRS2ValCSR = MuxCase(
        io.iInstCSRVal,
        Seq(
            (instName === INST_NAME_CSRRW) -> 0.U(DATA_WIDTH.W),
            (instName === INST_NAME_MRET)  -> (io.iInstCSRVal + 4.U(DATA_WIDTH.W))
        )
    )
    val aluRS2Val = MuxCase(
        0.U(DATA_WIDTH.W),
        Seq(
            (aluRS2 === ALU_RS2_X)     -> 0.U(DATA_WIDTH.W),
            (aluRS2 === ALU_RS2_R)     -> io.iInstRS2Val,
            (aluRS2 === ALU_RS2_CSR)   -> aluRS2ValCSR,
            (aluRS2 === ALU_RS2_IMM_I) -> instImmISext,
            (aluRS2 === ALU_RS2_IMM_S) -> instImmSSext,
            (aluRS2 === ALU_RS2_IMM_U) -> instImmUSext,
            (aluRS2 === ALU_RS2_IMM_J) -> instImmJSext
        )
    )

    io.oInstRDAddr := instRDAddr
    io.oInstRS1Val := io.iInstRS1Val
    io.oInstRS2Val := Mux(
        (instName === INST_NAME_BEQ)  ||
        (instName === INST_NAME_BNE)  ||
        (instName === INST_NAME_BLT)  ||
        (instName === INST_NAME_BGE)  ||
        (instName === INST_NAME_BLTU) ||
        (instName === INST_NAME_BGEU),
        instImmBSext,
        io.iInstRS2Val)
    io.oInstCSRVal := io.iInstCSRVal
    io.oInstImmVal := MuxCase(
        0.U(DATA_WIDTH.W),
        Seq(
            (aluRS2 === ALU_RS2_IMM_I) -> instImmISext,
            (aluRS2 === ALU_RS2_IMM_S) -> instImmSSext,
            (aluRS2 === ALU_RS2_IMM_U) -> instImmUSext,
            (aluRS2 === ALU_RS2_IMM_J) -> instImmJSext
        )
    )

    io.oInstName   := instName
    io.oALUType    := aluType
    io.oALURS1Val  := aluRS1Val
    io.oALURS2Val  := aluRS2Val
    io.oJmpEn      := jmpEn
    io.oMemWrEn    := memWrEn
    io.oMemByt     := memByt
    io.oRegWrEn    := regWrEn
    io.oRegWrSrc   := regWrSrc
}
