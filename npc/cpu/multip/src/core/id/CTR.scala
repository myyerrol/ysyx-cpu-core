package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.port._

class CTR extends Module with ConfigInstPattern {
    val io = IO(new Bundle {
        val iPC    = Input(UInt(DATA_WIDTH.W))
        val iInst  = Input(UInt(INST_WIDTH.W))

        val pCTR   = new CTRIO
    })

    var lInst = ListLookup(
        io.iInst,
        List(INST_NAME_X),
        Array(
            SLL    -> List(INST_NAME_SLL),
            SLLI   -> List(INST_NAME_SLLI),
            SRLI   -> List(INST_NAME_SRLI),
            SRA    -> List(INST_NAME_SRA),
            SRAI   -> List(INST_NAME_SRAI),
            SLLW   -> List(INST_NAME_SLLW),
            SLLIW  -> List(INST_NAME_SLLIW),
            SRLW   -> List(INST_NAME_SRLW),
            SRLIW  -> List(INST_NAME_SRLIW),
            SRAW   -> List(INST_NAME_SRAW),
            SRAIW  -> List(INST_NAME_SRAIW),

            ADD    -> List(INST_NAME_ADD),
            ADDI   -> List(INST_NAME_ADDI),
            SUB    -> List(INST_NAME_SUB),
            LUI    -> List(INST_NAME_LUI),
            AUIPC  -> List(INST_NAME_AUIPC),
            ADDW   -> List(INST_NAME_ADDW),
            ADDIW  -> List(INST_NAME_ADDIW),
            SUBW   -> List(INST_NAME_SUBW),

            XOR    -> List(INST_NAME_XOR),
            XORI   -> List(INST_NAME_XORI),
            OR     -> List(INST_NAME_OR),
            ORI    -> List(INST_NAME_ORI),
            AND    -> List(INST_NAME_AND),
            ANDI   -> List(INST_NAME_ANDI),

            SLT    -> List(INST_NAME_SLT),
            SLTU   -> List(INST_NAME_SLTU),
            SLTIU  -> List(INST_NAME_SLTIU),

            BEQ    -> List(INST_NAME_BEQ),
            BNE    -> List(INST_NAME_BNE),
            BLT    -> List(INST_NAME_BLT),
            BGE    -> List(INST_NAME_BGE),
            BLTU   -> List(INST_NAME_BLTU),
            BGEU   -> List(INST_NAME_BGEU),

            JAL    -> List(INST_NAME_JAL),
            JALR   -> List(INST_NAME_JALR),

            LB     -> List(INST_NAME_LB),
            LH     -> List(INST_NAME_LH),
            LBU    -> List(INST_NAME_LBU),
            LHU    -> List(INST_NAME_LHU),
            LW     -> List(INST_NAME_LW),
            LWU    -> List(INST_NAME_LWU),
            LD     -> List(INST_NAME_LD),

            SB     -> List(INST_NAME_SB),
            SH     -> List(INST_NAME_SH),
            SW     -> List(INST_NAME_SW),
            SD     -> List(INST_NAME_SD),

            EBREAK -> List(INST_NAME_EBREAK),

            MUL    -> List(INST_NAME_MUL),
            MULW   -> List(INST_NAME_MULW),
            DIVU   -> List(INST_NAME_DIVU),
            DIVW   -> List(INST_NAME_DIVW),
            DIVUW  -> List(INST_NAME_DIVUW),
            REMU   -> List(INST_NAME_REMU),
            REMW   -> List(INST_NAME_REMW)
        )
    )

    val wInstName = lInst(0)

    val rStateCurr = RegInit(STATE_RS)

    val wPCWrEn    = WireInit(EN_FALSE)
    val wPCWrConEn = WireInit(EN_FALSE)
    val wPCWrSrc   = WireInit(PC_WR_SRC_X)
    val wPCNextEn  = WireInit(EN_FALSE)
    val wPCJumpEn  = WireInit(EN_FALSE)
    val wMemRdEn   = WireInit(EN_FALSE)
    val wMemRdSrc  = WireInit(MEM_RD_SRC_X)
    val wMemWrEn   = WireInit(EN_FALSE)
    val wMemByt    = WireInit(MEM_BYT_X)
    val wIRWrEn    = WireInit(EN_FALSE)
    val wGPRWrEn   = WireInit(EN_FALSE)
    val wGPRWrSrc  = WireInit(GPR_WR_SRC_X)
    val wALUType   = WireInit(ALU_TYPE_X)
    val wALURS1    = WireInit(ALU_RS1_X)
    val wALURS2    = WireInit(ALU_RS2_X)

    switch (rStateCurr) {
        is (STATE_RS) {
            rStateCurr := STATE_IF
            // wMemRdEn   := EN_TRUE
            // wMemRdSrc  := MEM_RD_SRC_PC
        }
        is (STATE_IF) {
            rStateCurr := STATE_ME
            wPCNextEn  := EN_TRUE
            // wMemRdEn   := EN_TRUE
            // wMemRdSrc  := MEM_RD_SRC_PC
            wIRWrEn    := EN_TRUE
            wALUType   := ALU_TYPE_ADD
            wALURS1    := ALU_RS1_PC
            wALURS2    := ALU_RS2_4
        }
        is (STATE_ME) {
            rStateCurr := STATE_ID
            wMemRdEn   := EN_TRUE
            wMemRdSrc  := MEM_RD_SRC_PC
        }
        is (STATE_ID) {
            rStateCurr := STATE_EX
            when (wInstName === INST_NAME_BEQ  ||
                  wInstName === INST_NAME_BNE  ||
                  wInstName === INST_NAME_BLT  ||
                  wInstName === INST_NAME_BGE  ||
                  wInstName === INST_NAME_BLTU ||
                  wInstName === INST_NAME_BGEU) {
                wPCJumpEn := EN_TRUE
                wALUType  := ALU_TYPE_ADD
                wALURS1   := ALU_RS1_PC
                wALURS2   := ALU_RS2_IMM_B
            }
            when (wInstName === INST_NAME_JAL) {
                wPCJumpEn := EN_TRUE
                wALUType  := ALU_TYPE_ADD
                wALURS1   := ALU_RS1_PC
                wALURS2   := ALU_RS2_IMM_J
            }
        }
        is (STATE_EX) {
            rStateCurr := STATE_WB
            when (wInstName === INST_NAME_SLL   ||
                  wInstName === INST_NAME_SLLI  ||
                  wInstName === INST_NAME_SRLI  ||
                  wInstName === INST_NAME_SRA   ||
                  wInstName === INST_NAME_SRAI  ||
                  wInstName === INST_NAME_SLLW  ||
                  wInstName === INST_NAME_SLLIW ||
                  wInstName === INST_NAME_SRLW  ||
                  wInstName === INST_NAME_SRLIW ||
                  wInstName === INST_NAME_SRAW  ||
                  wInstName === INST_NAME_SRAIW) {
                wALUType := MuxLookup(
                    wInstName,
                    ALU_TYPE_X,
                    Seq(
                        INST_NAME_SLL   -> ALU_TYPE_SLL,
                        INST_NAME_SLLI  -> ALU_TYPE_SLL,
                        INST_NAME_SRLI  -> ALU_TYPE_SRL,
                        INST_NAME_SRA   -> ALU_TYPE_SRA,
                        INST_NAME_SRAI  -> ALU_TYPE_SRA,
                        INST_NAME_SLLW  -> ALU_TYPE_SLLW,
                        INST_NAME_SLLIW -> ALU_TYPE_SLL,
                        INST_NAME_SRLW  -> ALU_TYPE_SRLW,
                        INST_NAME_SRLIW -> ALU_TYPE_SRLIW,
                        INST_NAME_SRAW  -> ALU_TYPE_SRAW,
                        INST_NAME_SRAIW -> ALU_TYPE_SRAIW
                    )
                )
                wALURS1  := ALU_RS1_GPR
                wALURS2  := MuxLookup(
                    wInstName,
                    ALU_RS2_X,
                    Seq(
                        INST_NAME_SLL   -> ALU_RS2_GPR,
                        INST_NAME_SLLI  -> ALU_RS2_IMM_I,
                        INST_NAME_SRLI  -> ALU_RS2_IMM_I,
                        INST_NAME_SRA   -> ALU_RS2_GPR,
                        INST_NAME_SRAI  -> ALU_RS2_IMM_I,
                        INST_NAME_SLLW  -> ALU_RS2_GPR,
                        INST_NAME_SLLIW -> ALU_RS2_IMM_I,
                        INST_NAME_SRLW  -> ALU_RS2_GPR,
                        INST_NAME_SRLIW -> ALU_RS2_IMM_I,
                        INST_NAME_SRAW  -> ALU_RS2_GPR,
                        INST_NAME_SRAIW -> ALU_RS2_IMM_I
                    )
                )
            }
            .elsewhen (wInstName === INST_NAME_ADD   ||
                       wInstName === INST_NAME_ADDI  ||
                       wInstName === INST_NAME_SUB   ||
                       wInstName === INST_NAME_LUI   ||
                       wInstName === INST_NAME_AUIPC ||
                       wInstName === INST_NAME_ADDW  ||
                       wInstName === INST_NAME_ADDIW ||
                       wInstName === INST_NAME_SUBW) {
                wALUType := MuxLookup(
                    wInstName,
                    ALU_TYPE_X,
                    Seq(
                        INST_NAME_ADD   -> ALU_TYPE_ADD,
                        INST_NAME_ADDI  -> ALU_TYPE_ADD,
                        INST_NAME_SUB   -> ALU_TYPE_SUB,
                        INST_NAME_LUI   -> ALU_TYPE_ADD,
                        INST_NAME_AUIPC -> ALU_TYPE_ADD,
                        INST_NAME_ADDW  -> ALU_TYPE_ADD,
                        INST_NAME_ADDIW -> ALU_TYPE_ADD,
                        INST_NAME_SUBW  -> ALU_TYPE_SUB
                    )
                )
                wALURS1  := MuxLookup(
                    wInstName,
                    ALU_RS1_GPR,
                    Seq(
                        INST_NAME_LUI   -> ALU_RS1_X,
                        INST_NAME_AUIPC -> ALU_RS1_PC
                    )
                )
                wALURS2  := MuxLookup(
                    wInstName,
                    ALU_RS2_X,
                    Seq(
                        INST_NAME_ADD   -> ALU_RS2_GPR,
                        INST_NAME_ADDI  -> ALU_RS2_IMM_I,
                        INST_NAME_SUB   -> ALU_RS2_GPR,
                        INST_NAME_LUI   -> ALU_RS2_IMM_U,
                        INST_NAME_AUIPC -> ALU_RS2_IMM_U,
                        INST_NAME_ADDW  -> ALU_RS2_GPR,
                        INST_NAME_ADDIW -> ALU_RS2_IMM_I,
                        INST_NAME_SUBW  -> ALU_RS2_GPR
                    )
                )
            }
            .elsewhen (wInstName === INST_NAME_XOR  ||
                       wInstName === INST_NAME_XORI ||
                       wInstName === INST_NAME_OR   ||
                       wInstName === INST_NAME_ORI  ||
                       wInstName === INST_NAME_AND  ||
                       wInstName === INST_NAME_ANDI) {
                wALUType := MuxLookup(
                    wInstName,
                    ALU_TYPE_X,
                    Seq(
                        INST_NAME_XOR  -> ALU_TYPE_XOR,
                        INST_NAME_XORI -> ALU_TYPE_XOR,
                        INST_NAME_OR   -> ALU_TYPE_OR,
                        INST_NAME_ORI  -> ALU_TYPE_OR,
                        INST_NAME_AND  -> ALU_TYPE_AND,
                        INST_NAME_ANDI -> ALU_TYPE_AND
                    )
                )
                wALURS1  := ALU_RS1_GPR
                wALURS2  := MuxLookup(
                    wInstName,
                    ALU_RS2_X,
                    Seq(
                        INST_NAME_XOR  -> ALU_RS2_GPR,
                        INST_NAME_XORI -> ALU_RS2_IMM_I,
                        INST_NAME_OR   -> ALU_RS2_GPR,
                        INST_NAME_ORI  -> ALU_RS2_IMM_I,
                        INST_NAME_AND  -> ALU_RS2_GPR,
                        INST_NAME_ANDI -> ALU_RS2_IMM_I
                    )
                )
            }
            .elsewhen (wInstName === INST_NAME_SLT  ||
                       wInstName === INST_NAME_SLTU ||
                       wInstName === INST_NAME_SLTIU) {
                wALUType := Mux(wInstName === INST_NAME_SLT, ALU_TYPE_SLT,
                                                             ALU_TYPE_SLTU)
                wALURS1  := ALU_RS1_GPR
                wALURS2  := Mux(wInstName === INST_NAME_SLTIU, ALU_RS2_IMM_I,
                                                               ALU_RS2_GPR)
            }
            .elsewhen (wInstName === INST_NAME_BEQ  ||
                       wInstName === INST_NAME_BNE  ||
                       wInstName === INST_NAME_BLT  ||
                       wInstName === INST_NAME_BGE  ||
                       wInstName === INST_NAME_BLTU ||
                       wInstName === INST_NAME_BGEU) {
                rStateCurr := STATE_IF
                wPCWrEn  := EN_TRUE
                wPCWrSrc := PC_WR_SRC_JUMP
                wALUType := MuxLookup(
                    wInstName,
                    ALU_TYPE_X,
                    Seq(
                        INST_NAME_BEQ  -> ALU_TYPE_BEQ,
                        INST_NAME_BNE  -> ALU_TYPE_BNE,
                        INST_NAME_BLT  -> ALU_TYPE_BLT,
                        INST_NAME_BGE  -> ALU_TYPE_BGE,
                        INST_NAME_BLTU -> ALU_TYPE_BLTU,
                        INST_NAME_BGEU -> ALU_TYPE_BGEU
                    )
                )
                wALURS1  := ALU_RS1_GPR
                wALURS2  := ALU_RS2_GPR
            }
            .elsewhen (wInstName === INST_NAME_JAL) {
                wALUType := ALU_TYPE_ADD
                wALURS1  := ALU_RS1_PC
                wALURS2  := ALU_RS2_4
            }
            .elsewhen (wInstName === INST_NAME_JALR) {
                rStateCurr := STATE_LS
                wPCJumpEn  := EN_TRUE
                wALUType   := ALU_TYPE_JALR
                wALURS1    := ALU_RS1_GPR
                wALURS2    := ALU_RS2_IMM_I
            }
            .elsewhen (wInstName === INST_NAME_LB  ||
                       wInstName === INST_NAME_LH  ||
                       wInstName === INST_NAME_LBU ||
                       wInstName === INST_NAME_LHU ||
                       wInstName === INST_NAME_LW  ||
                       wInstName === INST_NAME_LWU ||
                       wInstName === INST_NAME_LD) {
                rStateCurr := STATE_LS
                wALUType   := ALU_TYPE_ADD
                wALURS1    := ALU_RS1_GPR
                wALURS2    := ALU_RS2_IMM_I
            }
            .elsewhen (wInstName === INST_NAME_SB ||
                       wInstName === INST_NAME_SH ||
                       wInstName === INST_NAME_SW ||
                       wInstName === INST_NAME_SD) {
                rStateCurr := STATE_LS
                wALUType   := ALU_TYPE_ADD
                wALURS1    := ALU_RS1_GPR
                wALURS2    := ALU_RS2_IMM_S
            }
            .elsewhen (wInstName === INST_NAME_MUL   ||
                       wInstName === INST_NAME_MULW  ||
                       wInstName === INST_NAME_DIVU  ||
                       wInstName === INST_NAME_DIVW  ||
                       wInstName === INST_NAME_DIVUW ||
                       wInstName === INST_NAME_REMU  ||
                       wInstName === INST_NAME_REMW) {
                wALUType := MuxLookup(
                    wInstName,
                    ALU_TYPE_X,
                    Seq(
                        INST_NAME_MUL   -> ALU_TYPE_MUL,
                        INST_NAME_MULW  -> ALU_TYPE_MUL,
                        INST_NAME_DIVU  -> ALU_TYPE_DIVU,
                        INST_NAME_DIVW  -> ALU_TYPE_DIVW,
                        INST_NAME_DIVUW -> ALU_TYPE_DIVUW,
                        INST_NAME_REMU  -> ALU_TYPE_REMU,
                        INST_NAME_REMW  -> ALU_TYPE_REMW
                    )
                )
                wALURS1  := ALU_RS1_GPR
                wALURS2  := ALU_RS2_GPR
            }
        }
        is (STATE_LS) {
            when (wInstName === INST_NAME_JALR) {
                rStateCurr := STATE_WB
                wALUType   := ALU_TYPE_ADD
                wALURS1    := ALU_RS1_PC
                wALURS2    := ALU_RS2_4
            }
            .elsewhen (wInstName === INST_NAME_LB  ||
                       wInstName === INST_NAME_LH  ||
                       wInstName === INST_NAME_LBU ||
                       wInstName === INST_NAME_LHU ||
                       wInstName === INST_NAME_LW  ||
                       wInstName === INST_NAME_LWU ||
                       wInstName === INST_NAME_LD) {
                rStateCurr := STATE_WB
                wMemRdEn   := EN_TRUE
                wMemRdSrc  := MEM_RD_SRC_ALU
            }
            .elsewhen (wInstName === INST_NAME_SB ||
                       wInstName === INST_NAME_SH ||
                       wInstName === INST_NAME_SW ||
                       wInstName === INST_NAME_SD) {
                rStateCurr := STATE_IF
                wPCWrEn    := EN_TRUE
                wPCWrSrc   := PC_WR_SRC_NEXT
                wMemWrEn   := EN_TRUE
                wMemByt    := MuxLookup(
                    wInstName,
                    MEM_BYT_X,
                    Seq(
                        INST_NAME_SB -> MEM_BYT_1_U,
                        INST_NAME_SH -> MEM_BYT_2_U,
                        INST_NAME_SW -> MEM_BYT_4_U,
                        INST_NAME_SD -> MEM_BYT_8_U
                    )
                )
                wALURS2    := ALU_RS2_GPR
            }
        }
        is (STATE_WB) {
            rStateCurr := STATE_IF
            wPCWrEn    := EN_TRUE
            wPCWrSrc   := PC_WR_SRC_NEXT
            wGPRWrEn   := EN_TRUE
            wGPRWrSrc  := GPR_WR_SRC_ALU

            when (wInstName === INST_NAME_JAL ||
                  wInstName === INST_NAME_JALR) {
                wPCWrSrc := PC_WR_SRC_JUMP
            }
            .elsewhen (wInstName === INST_NAME_LB  ||
                       wInstName === INST_NAME_LH  ||
                       wInstName === INST_NAME_LBU ||
                       wInstName === INST_NAME_LHU ||
                       wInstName === INST_NAME_LW  ||
                       wInstName === INST_NAME_LWU ||
                       wInstName === INST_NAME_LD) {
                wMemByt   := MuxLookup(
                    wInstName,
                    MEM_BYT_X,
                    Seq(
                        INST_NAME_LB  -> MEM_BYT_1_S,
                        INST_NAME_LH  -> MEM_BYT_2_S,
                        INST_NAME_LBU -> MEM_BYT_1_U,
                        INST_NAME_LHU -> MEM_BYT_2_U,
                        INST_NAME_LW  -> MEM_BYT_4_S,
                        INST_NAME_LWU -> MEM_BYT_4_U,
                        INST_NAME_LD  -> MEM_BYT_8_S
                    )
                )
                wGPRWrSrc := GPR_WR_SRC_MEM
            }
        }
    }

    io.pCTR.oInstName  := wInstName
    io.pCTR.oStateCurr := rStateCurr
    io.pCTR.oPCWrEn    := wPCWrEn
    io.pCTR.oPCWrSrc   := wPCWrSrc
    io.pCTR.oPCNextEn  := wPCNextEn
    io.pCTR.oPCJumpEn  := wPCJumpEn
    io.pCTR.oMemRdEn   := wMemRdEn
    io.pCTR.oMemRdSrc  := wMemRdSrc
    io.pCTR.oMemWrEn   := wMemWrEn
    io.pCTR.oMemByt    := wMemByt
    io.pCTR.oIRWrEn    := wIRWrEn
    io.pCTR.oGPRWrEn   := wGPRWrEn
    io.pCTR.oGPRWrSrc  := wGPRWrSrc
    io.pCTR.oALUType   := wALUType
    io.pCTR.oALURS1    := wALURS1
    io.pCTR.oALURS2    := wALURS2
}
