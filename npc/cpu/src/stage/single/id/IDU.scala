package cpu.stage.single

import chisel3._
import chisel3.util._

import cpu.common._

class IDU extends Module with ConfigInstPattern {
    val io = IO(new Bundle {
        val iInst        = Input (UInt(INST_WIDTH.W))
        val iInstRS1Val  = Input (UInt(DATA_WIDTH.W))
        val iInstRS2Val  = Input (UInt(DATA_WIDTH.W))
        val iInstCSRVal  = Input (UInt(DATA_WIDTH.W))
        val iPC          = Input (UInt(ADDR_WIDTH.W))

        val oInstRS1Addr = Output(UInt(GPRS_WIDTH.W))
        val oInstRS2Addr = Output(UInt(GPRS_WIDTH.W))
        val oInstRDAddr  = Output(UInt(GPRS_WIDTH.W))
        val oInstCSRAddr = Output(UInt(ADDR_WIDTH.W))
        val oInstRS1Val  = Output(UInt(DATA_WIDTH.W))
        val oInstRS2Val  = Output(UInt(DATA_WIDTH.W))
        val oInstCSRVal  = Output(UInt(DATA_WIDTH.W))
        val oInstImmVal  = Output(UInt(DATA_WIDTH.W))

        val oInstName    = Output(UInt(SIGS_WIDTH.W))
        val oALUType     = Output(UInt(SIGS_WIDTH.W))
        val oALURS1      = Output(UInt(SIGS_WIDTH.W))
        val oALURS2      = Output(UInt(SIGS_WIDTH.W))
        val oALURS1Val   = Output(UInt(DATA_WIDTH.W))
        val oALURS2Val   = Output(UInt(DATA_WIDTH.W))
        val oJmpEn       = Output(Bool())
        val oMemWrEn     = Output(Bool())
        val oMemByt      = Output(UInt(SIGS_WIDTH.W))
        val oGPRWrEn     = Output(Bool())
        val oGPRWrSrc    = Output(UInt(SIGS_WIDTH.W))
    })

    val wInst = io.iInst;
    var lInst = ListLookup(
        wInst,
        List(INST_NAME_X, ALU_TYPE_X, ALU_RS1_X, ALU_RS2_X, JMP_FL, MEM_WR_FL, MEM_BYT_X, GPR_WR_FL, GPR_WR_SRC_X),
        Array(
            SLL    -> List(INST_NAME_SLL,    ALU_TYPE_SLL,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            SLLI   -> List(INST_NAME_SLLI,   ALU_TYPE_SLL,   ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            SRL    -> List(INST_NAME_SRL,    ALU_TYPE_SRL,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            SRLI   -> List(INST_NAME_SRLI,   ALU_TYPE_SRL,   ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            SRA    -> List(INST_NAME_SRA,    ALU_TYPE_SRA,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            SRAI   -> List(INST_NAME_SRAI,   ALU_TYPE_SRA,   ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            SLLW   -> List(INST_NAME_SLLW,   ALU_TYPE_SLLW,  ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            SLLIW  -> List(INST_NAME_SLLIW,  ALU_TYPE_SLL,   ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            SRLW   -> List(INST_NAME_SRLW,   ALU_TYPE_SRLW,  ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            SRLIW  -> List(INST_NAME_SRLIW,  ALU_TYPE_SRLIW, ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            SRAW   -> List(INST_NAME_SRAW,   ALU_TYPE_SRAW,  ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            SRAIW  -> List(INST_NAME_SRAIW,  ALU_TYPE_SRAIW, ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),

            ADD    -> List(INST_NAME_ADD,    ALU_TYPE_ADD,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            ADDI   -> List(INST_NAME_ADDI,   ALU_TYPE_ADD,   ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            SUB    -> List(INST_NAME_SUB,    ALU_TYPE_SUB,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            LUI    -> List(INST_NAME_LUI,    ALU_TYPE_ADD,   ALU_RS1_X,    ALU_RS2_IMM_U, JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            AUIPC  -> List(INST_NAME_AUIPC,  ALU_TYPE_ADD,   ALU_RS1_PC,   ALU_RS2_IMM_U, JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            ADDW   -> List(INST_NAME_ADDW,   ALU_TYPE_ADD,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            ADDIW  -> List(INST_NAME_ADDIW,  ALU_TYPE_ADD,   ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            SUBW   -> List(INST_NAME_SUBW,   ALU_TYPE_SUB,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),

            XOR    -> List(INST_NAME_XOR,    ALU_TYPE_XOR,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            XORI   -> List(INST_NAME_XORI,   ALU_TYPE_XOR,   ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            OR     -> List(INST_NAME_OR,     ALU_TYPE_OR,    ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            ORI    -> List(INST_NAME_ORI,    ALU_TYPE_OR,    ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            AND    -> List(INST_NAME_AND,    ALU_TYPE_AND,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            ANDI   -> List(INST_NAME_ANDI,   ALU_TYPE_AND,   ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),

            SLT    -> List(INST_NAME_SLT,    ALU_TYPE_SLT,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            SLTI   -> List(INST_NAME_SLTI,   ALU_TYPE_SLT,   ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            SLTU   -> List(INST_NAME_SLTU,   ALU_TYPE_SLTU,  ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            SLTIU  -> List(INST_NAME_SLTIU,  ALU_TYPE_SLTU,  ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),

            BEQ    -> List(INST_NAME_BEQ,    ALU_TYPE_BEQ,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_FL, GPR_WR_SRC_X),
            BNE    -> List(INST_NAME_BNE,    ALU_TYPE_BNE,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_FL, GPR_WR_SRC_X),
            BLT    -> List(INST_NAME_BLT,    ALU_TYPE_BLT,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_FL, GPR_WR_SRC_X),
            BGE    -> List(INST_NAME_BGE,    ALU_TYPE_BGE,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_FL, GPR_WR_SRC_X),
            BLTU   -> List(INST_NAME_BLTU,   ALU_TYPE_BLTU,  ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_FL, GPR_WR_SRC_X),
            BGEU   -> List(INST_NAME_BGEU,   ALU_TYPE_BGEU,  ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_FL, GPR_WR_SRC_X),

            JAL    -> List(INST_NAME_JAL,    ALU_TYPE_ADD,   ALU_RS1_PC,   ALU_RS2_IMM_J, JMP_TR, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_PC),
            JALR   -> List(INST_NAME_JALR,   ALU_TYPE_JALR,  ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_TR, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_PC),

            LB     -> List(INST_NAME_LB,     ALU_TYPE_ADD,   ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_1_S, GPR_WR_TR, GPR_WR_SRC_MEM),
            LH     -> List(INST_NAME_LH,     ALU_TYPE_ADD,   ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_2_S, GPR_WR_TR, GPR_WR_SRC_MEM),
            LBU    -> List(INST_NAME_LBU,    ALU_TYPE_ADD,   ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_1_U, GPR_WR_TR, GPR_WR_SRC_MEM),
            LHU    -> List(INST_NAME_LHU,    ALU_TYPE_ADD,   ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_2_U, GPR_WR_TR, GPR_WR_SRC_MEM),
            LW     -> List(INST_NAME_LW,     ALU_TYPE_ADD,   ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_4_S, GPR_WR_TR, GPR_WR_SRC_MEM),
            LWU    -> List(INST_NAME_LWU,    ALU_TYPE_ADD,   ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_4_U, GPR_WR_TR, GPR_WR_SRC_MEM),
            LD     -> List(INST_NAME_LD,     ALU_TYPE_ADD,   ALU_RS1_GPR,  ALU_RS2_IMM_I, JMP_FL, MEM_WR_FL, MEM_BYT_8_S, GPR_WR_TR, GPR_WR_SRC_MEM),

            SB     -> List(INST_NAME_SB,     ALU_TYPE_ADD,   ALU_RS1_GPR,  ALU_RS2_IMM_S, JMP_FL, MEM_WR_TR, MEM_BYT_1_U, GPR_WR_FL, GPR_WR_SRC_X),
            SH     -> List(INST_NAME_SH,     ALU_TYPE_ADD,   ALU_RS1_GPR,  ALU_RS2_IMM_S, JMP_FL, MEM_WR_TR, MEM_BYT_2_U, GPR_WR_FL, GPR_WR_SRC_X),
            SW     -> List(INST_NAME_SW,     ALU_TYPE_ADD,   ALU_RS1_GPR,  ALU_RS2_IMM_S, JMP_FL, MEM_WR_TR, MEM_BYT_4_U, GPR_WR_FL, GPR_WR_SRC_X),
            SD     -> List(INST_NAME_SD,     ALU_TYPE_ADD,   ALU_RS1_GPR,  ALU_RS2_IMM_S, JMP_FL, MEM_WR_TR, MEM_BYT_8_U, GPR_WR_FL, GPR_WR_SRC_X),

            ECALL  -> List(INST_NAME_ECALL,  ALU_TYPE_X,     ALU_RS1_X,    ALU_RS2_X,     JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_FL, GPR_WR_SRC_X),
            EBREAK -> List(INST_NAME_EBREAK, ALU_TYPE_X,     ALU_RS1_X,    ALU_RS2_X,     JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_FL, GPR_WR_SRC_X),

            CSRRW  -> List(INST_NAME_CSRRW,  ALU_TYPE_OR,    ALU_RS1_GPR,  ALU_RS2_CSR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_CSR),
            CSRRS  -> List(INST_NAME_CSRRS,  ALU_TYPE_OR,    ALU_RS1_GPR,  ALU_RS2_CSR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_CSR),
            MRET   -> List(INST_NAME_MRET,   ALU_TYPE_ADD,   ALU_RS1_X,    ALU_RS2_CSR,   JMP_TR, MEM_WR_FL, MEM_BYT_X,   GPR_WR_FL, GPR_WR_SRC_X),

            MUL    -> List(INST_NAME_MUL,    ALU_TYPE_MUL,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            MULH   -> List(INST_NAME_MULH,   ALU_TYPE_MULH,  ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            MULHSU -> List(INST_NAME_MULHSU, ALU_TYPE_MULHSU, ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            MULHU  -> List(INST_NAME_MULHU,  ALU_TYPE_MULHU,  ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            MULW   -> List(INST_NAME_MULW,   ALU_TYPE_MUL,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            DIV    -> List(INST_NAME_DIV,    ALU_TYPE_DIV,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            DIVU   -> List(INST_NAME_DIVU,   ALU_TYPE_DIVU,  ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            DIVW   -> List(INST_NAME_DIVW,   ALU_TYPE_DIVW,  ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            DIVUW  -> List(INST_NAME_DIVUW,  ALU_TYPE_DIVUW, ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            REM    -> List(INST_NAME_REM,    ALU_TYPE_REM,   ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            REMU   -> List(INST_NAME_REMU,   ALU_TYPE_REMU,  ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            REMW   -> List(INST_NAME_REMW,   ALU_TYPE_REMW,  ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
            REMUW  -> List(INST_NAME_REMUW,  ALU_TYPE_REMUW, ALU_RS1_GPR,  ALU_RS2_GPR,   JMP_FL, MEM_WR_FL, MEM_BYT_X,   GPR_WR_TR, GPR_WR_SRC_ALU),
        )
    )
    val wInstName = lInst(0)
    val wALUType  = lInst(1)
    val wALURS1   = lInst(2)
    val wALURS2   = lInst(3)
    val wJmpEn    = lInst(4)
    val wMemWrEn  = lInst(5)
    val wMemByt   = lInst(6)
    val wGPRWrEn  = lInst(7)
    val wGPRWrSrc = lInst(8)

    when (wInstName === INST_NAME_X) {
        assert(false.B, "Invalid instruction at 0x%x", io.iPC)
    }

    val instRS1Addr  = wInst(19, 15)
    val instRS2Addr  = wInst(24, 20)
    val instRDAddr   = wInst(11, 7)

    val wImmI      = wInst(31, 20)
    val wImmIExten = SignExten(wImmI, DATA_WIDTH)
    val wImmS      = Cat(wInst(31, 25), wInst(11, 7))
    val wImmSExten = SignExten(wImmS, DATA_WIDTH)
    val wImmB      = Cat(wInst(31), wInst(7), wInst(30, 25), wInst(11, 8), 0.U(1.W))
    val wImmBExten = SignExten(wImmB, DATA_WIDTH)
    val wImmU      = Cat(wInst(31, 12), 0.U(12.W))
    val wImmUExten = SignExten(wImmU, DATA_WIDTH)
    val wImmJ      = Cat(wInst(31), wInst(19, 12), wInst(20), wInst(30, 21), 0.U(1.W))
    val wImmJExten = SignExten(wImmJ, DATA_WIDTH)

    io.oInstRS1Addr := instRS1Addr
    io.oInstRS2Addr := instRS2Addr
    io.oInstCSRAddr:= MuxCase(
        0.U(ADDR_WIDTH.W),
        Seq((wInstName === INST_NAME_ECALL)  -> CSR_MTVEC,
           ((wInstName === INST_NAME_CSRRW) ||
            (wInstName === INST_NAME_CSRRS)) -> wImmIExten,
            (wInstName === INST_NAME_MRET)   -> CSR_MEPC
        )
    )

    val aluRS1Val = MuxCase(
        0.U(DATA_WIDTH.W),
        Seq(
            (wALURS1 === ALU_RS1_X)   -> 0.U(DATA_WIDTH.W),
            (wALURS1 === ALU_RS1_GPR) -> io.iInstRS1Val,
            (wALURS1 === ALU_RS1_PC)  -> io.iPC
        )
    )
    val aluRS2ValCSR = MuxCase(
        io.iInstCSRVal,
        Seq(
            (wInstName === INST_NAME_CSRRW) -> 0.U(DATA_WIDTH.W),
            (wInstName === INST_NAME_MRET)  -> (io.iInstCSRVal + 4.U(DATA_WIDTH.W))
        )
    )
    val aluRS2Val = MuxCase(
        0.U(DATA_WIDTH.W),
        Seq(
            (wALURS2 === ALU_RS2_X)     -> 0.U(DATA_WIDTH.W),
            (wALURS2 === ALU_RS2_GPR)   -> io.iInstRS2Val,
            (wALURS2 === ALU_RS2_CSR)   -> aluRS2ValCSR,
            (wALURS2 === ALU_RS2_IMM_I) -> wImmIExten,
            (wALURS2 === ALU_RS2_IMM_S) -> wImmSExten,
            (wALURS2 === ALU_RS2_IMM_U) -> wImmUExten,
            (wALURS2 === ALU_RS2_IMM_J) -> wImmJExten
        )
    )

    io.oInstRDAddr := instRDAddr
    io.oInstRS1Val := io.iInstRS1Val
    io.oInstRS2Val := Mux(
        (wInstName === INST_NAME_BEQ)  ||
        (wInstName === INST_NAME_BNE)  ||
        (wInstName === INST_NAME_BLT)  ||
        (wInstName === INST_NAME_BGE)  ||
        (wInstName === INST_NAME_BLTU) ||
        (wInstName === INST_NAME_BGEU),
        wImmBExten,
        io.iInstRS2Val)
    io.oInstCSRVal := io.iInstCSRVal
    io.oInstImmVal := MuxCase(
        0.U(DATA_WIDTH.W),
        Seq(
            (wALURS2 === ALU_RS2_IMM_I) -> wImmIExten,
            (wALURS2 === ALU_RS2_IMM_S) -> wImmSExten,
            (wALURS2 === ALU_RS2_IMM_U) -> wImmUExten,
            (wALURS2 === ALU_RS2_IMM_J) -> wImmJExten
        )
    )

    io.oInstName   := wInstName
    io.oALUType    := wALUType
    io.oALURS1     := wALURS1
    io.oALURS2     := wALURS2
    io.oALURS1Val  := aluRS1Val
    io.oALURS2Val  := aluRS2Val
    io.oJmpEn      := wJmpEn
    io.oMemWrEn    := wMemWrEn
    io.oMemByt     := wMemByt
    io.oGPRWrEn    := wGPRWrEn
    io.oGPRWrSrc   := wGPRWrSrc
}
