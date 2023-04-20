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
        val iPC          = Input(UInt(DATA_WIDTH.W))

        val oInstRS1Addr = Output(UInt(REG_WIDTH.W))
        val oInstRS2Addr = Output(UInt(REG_WIDTH.W))
        val oInstRDAddr  = Output(UInt(REG_WIDTH.W))
        val oInstRS1Val  = Output(UInt(DATA_WIDTH.W))
        val oInstRS2Val  = Output(UInt(DATA_WIDTH.W))
        val oALUType     = Output(UInt(SIGNAL_WIDTH.W))
        val oALURS1Val   = Output(UInt(DATA_WIDTH.W))
        val oALURS2Val   = Output(UInt(DATA_WIDTH.W))
        val oJmpEn       = Output(Bool())
        val oMemWrEn     = Output(Bool())
        val oRegWrEn     = Output(Bool())
        val oRegWrSrc    = Output(UInt(SIGNAL_WIDTH.W))
        val oCsrWrEn     = Output(Bool())
    })

    val inst = io.iInst;
    var signals = ListLookup(
        inst,
        List(ALU_TYPE_X, ALU_RS1_X, ALU_RS2_X, JMP_F, MEM_WR_F, REG_WR_F, REG_WR_SRC_X, CSR_WR_F),
        Array(
            LUI    -> List(ALU_TYPE_ADD,    ALU_RS1_X,  ALU_RS2_IMM_U, JMP_F, MEM_WR_F, REG_WR_T, REG_WR_SRC_ALU, CSR_WR_F),
            AUIPC  -> List(ALU_TYPE_ADD,    ALU_RS1_PC, ALU_RS2_IMM_U, JMP_F, MEM_WR_F, REG_WR_T, REG_WR_SRC_ALU, CSR_WR_F),
            JAL    -> List(ALU_TYPE_ADD,    ALU_RS1_PC, ALU_RS2_IMM_J, JMP_T, MEM_WR_F, REG_WR_T, REG_WR_SRC_PC,  CSR_WR_F),
            JALR   -> List(ALU_TYPE_JALR,   ALU_RS1_R,  ALU_RS2_IMM_I, JMP_T, MEM_WR_F, REG_WR_T, REG_WR_SRC_PC,  CSR_WR_F),
            SD     -> List(ALU_TYPE_ADD,    ALU_RS1_R,  ALU_RS2_IMM_S, JMP_F, MEM_WR_T, REG_WR_F, REG_WR_SRC_X,   CSR_WR_F),
            ADDI   -> List(ALU_TYPE_ADD,    ALU_RS1_R,  ALU_RS2_IMM_I, JMP_F, MEM_WR_F, REG_WR_T, REG_WR_SRC_ALU, CSR_WR_F),
            EBREAK -> List(ALU_TYPE_EBREAK, ALU_RS1_X,  ALU_RS2_X,     JMP_F, MEM_WR_F, REG_WR_F, REG_WR_SRC_X,   CSR_WR_F))
    )
    val aluType  = signals(0)
    val aluRS1   = signals(1)
    val aluRS2   = signals(2)
    val jmp      = signals(3)
    val memWr    = signals(4)
    val regWr    = signals(5)
    val regWrSrc = signals(6)
    val csrWr    = signals(7)

    when (aluType === ALU_TYPE_X) {
        assert(false.B, "Invalid instruction at 0x%x", io.iPC)
    }

    val instRS1Addr  = inst(19, 15)
    val instRS2Addr  = inst(24, 20)
    val instRDAddr   = inst(11, 7)
    val instImmI     = inst(31, 20)
    val instImmISext = Cat(Fill(52, instImmI(11)), instImmI)
    val instImmS     = Cat(inst(31, 25), inst(11, 7))
    val instImmSSext = Cat(Fill(52, instImmS(11)), instImmS)
    val instImmU     = inst(31, 12)
    val instImmUSext = Cat(Fill(32, instImmU(19)), instImmU, 0.U(12.U))
    val instImmJ     = Cat(inst(31), inst(19, 12), inst(20), inst(30, 21))
    val instImmJSext = Cat(Fill(43, instImmJ(19)), instImmJ, 0.U(1.U))

    io.oInstRS1Addr := instRS1Addr
    io.oInstRS2Addr := instRS2Addr

    val aluRS1Val = MuxCase(
        0.U(DATA_WIDTH.W),
        Seq(
            (aluRS1 === ALU_RS1_X)  -> 0.U(DATA_WIDTH.W),
            (aluRS1 === ALU_RS1_R)  -> io.iInstRS1Val,
            (aluRS1 === ALU_RS1_PC) -> io.iPC
        )
    )
    val aluRS2Val = MuxCase(
        0.U(DATA_WIDTH.W),
        Seq(
            (aluRS2 === ALU_RS2_X)     -> 0.U(DATA_WIDTH.W),
            (aluRS2 === ALU_RS2_R)     -> io.iInstRS2Val,
            (aluRS2 === ALU_RS2_IMM_I) -> instImmISext,
            (aluRS2 === ALU_RS2_IMM_S) -> instImmSSext,
            (aluRS2 === ALU_RS2_IMM_U) -> instImmUSext,
            (aluRS2 === ALU_RS2_IMM_J) -> instImmJSext
        )
    )

    io.oInstRDAddr := instRDAddr
    io.oInstRS1Val := io.iInstRS1Val
    io.oInstRS2Val := io.iInstRS2Val
    io.oALUType    := aluType
    io.oALURS1Val  := aluRS1Val
    io.oALURS2Val  := aluRS2Val
    io.oJmpEn      := jmp
    io.oMemWrEn    := memWr
    io.oRegWrEn    := regWr
    io.oRegWrSrc   := regWrSrc
    io.oCsrWrEn    := csrWr
}
