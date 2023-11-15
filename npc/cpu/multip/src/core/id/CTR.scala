package cpu.core

import chisel3._
import chisel3.util._

import cpu.core._

class CTR extends Module with ConfigInstPattern {
    val io = IO(new Bundle {
        val iPC   = Input(UInt(DATA_WIDTH.W))
        val iInst = Input(UInt(DATA_WIDTH.W))

        val oInstName   = Output(UInt(SIGS_WIDTH.W))
        val oIRWrEn     = Output(UInt(SIGS_WIDTH.W))
        val oALUType    = Output(UInt(SIGS_WIDTH.W))
        val oALURS1     = Output(UInt(SIGS_WIDTH.W))
        val oALURS2     = Output(UInt(SIGS_WIDTH.W))
        val oMemWrEn    = Output(UInt(SIGS_WIDTH.W))
        val oMemRdEn    = Output(UInt(SIGS_WIDTH.W))
        val oMemByt     = Output(UInt(SIGS_WIDTH.W))
        val oMemAddrSrc = Output(UInt(SIGS_WIDTH.W))
        val oGPRWrEn    = Output(UInt(SIGS_WIDTH.W))
        val oGPRWrSrc   = Output(UInt(SIGS_WIDTH.W))
        val oPCWrEn     = Output(UInt(SIGS_WIDTH.W))
        val oPCWrConEn  = Output(UInt(SIGS_WIDTH.W))
        val oPCWrSrc    = Output(UInt(SIGS_WIDTH.W))
    })

    var lInst = ListLookup(
        io.iInst,
        List(INST_NAME_X),
        Array(
            SLL    -> List(INST_NAME_SLL  ),
            SLLI   -> List(INST_NAME_SLLI ),
            SRLI   -> List(INST_NAME_SRLI ),
            SRA    -> List(INST_NAME_SRA  ),
            SRAI   -> List(INST_NAME_SRAI ),
            SLLW   -> List(INST_NAME_SLLW ),
            SLLIW  -> List(INST_NAME_SLLIW),
            SRLW   -> List(INST_NAME_SRLW ),
            SRLIW  -> List(INST_NAME_SRLIW),
            SRAW   -> List(INST_NAME_SRAW ),
            SRAIW  -> List(INST_NAME_SRAIW),

            ADD    -> List(INST_NAME_ADD  ),
            ADDI   -> List(INST_NAME_ADDI ),
            SUB    -> List(INST_NAME_SUB  ),
            LUI    -> List(INST_NAME_LUI  ),
            AUIPC  -> List(INST_NAME_AUIPC),
            ADDW   -> List(INST_NAME_ADDW ),
            ADDIW  -> List(INST_NAME_ADDIW),
            SUBW   -> List(INST_NAME_SUBW ),

            XOR    -> List(INST_NAME_XOR ),
            XORI   -> List(INST_NAME_XORI),
            OR     -> List(INST_NAME_OR  ),
            ORI    -> List(INST_NAME_ORI ),
            AND    -> List(INST_NAME_AND ),
            ANDI   -> List(INST_NAME_ANDI),

            SLT    -> List(INST_NAME_SLT  ),
            SLTU   -> List(INST_NAME_SLTU ),
            SLTIU  -> List(INST_NAME_SLTIU),

            BEQ    -> List(INST_NAME_BEQ ),
            BNE    -> List(INST_NAME_BNE ),
            BLT    -> List(INST_NAME_BLT ),
            BGE    -> List(INST_NAME_BGE ),
            BLTU   -> List(INST_NAME_BLTU),
            BGEU   -> List(INST_NAME_BGEU),

            JAL    -> List(INST_NAME_JAL ),
            JALR   -> List(INST_NAME_JALR),

            LB     -> List(INST_NAME_LB ),
            LH     -> List(INST_NAME_LH ),
            LBU    -> List(INST_NAME_LBU),
            LHU    -> List(INST_NAME_LHU),
            LW     -> List(INST_NAME_LW ),
            LWU    -> List(INST_NAME_LWU),
            LD     -> List(INST_NAME_LD ),

            SB     -> List(INST_NAME_SB),
            SH     -> List(INST_NAME_SH),
            SW     -> List(INST_NAME_SW),
            SD     -> List(INST_NAME_SD),

            EBREAK -> List(INST_NAME_EBREAK),

            MUL    -> List(INST_NAME_MUL  ),
            MULW   -> List(INST_NAME_MULW ),
            DIVU   -> List(INST_NAME_DIVU ),
            DIVW   -> List(INST_NAME_DIVW ),
            DIVUW  -> List(INST_NAME_DIVUW),
            REMU   -> List(INST_NAME_REMU ),
            REMW   -> List(INST_NAME_REMW ))
    )

    val wInstName = lInst(0)
    when (wInstName === INST_NAME_X) {
        assert(false.B, "Invalid instruction at 0x%x", io.iPC)
    }

    io.oInstName := wInstName

    val STATE_RS ::
        STATE_IF ::
        STATE_ID ::
        STATE_EX ::
        STATE_LS ::
        STATE_WB :: Nil = Enum(6)
    val rStateCurr = RegInit(STATE_RS)

    switch (rStateCurr) {
        is (STATE_RS) {
            rStateCurr     := STATE_IF

            io.oIRWrEn     := SIGS_FALSE
            io.oALUType    := ALU_TYPE_X
            io.oALURS1     := ALU_RS1_X
            io.oALURS2     := ALU_RS2_X
            io.oMemWrEn    := SIGS_FALSE
            io.oMemRdEn    := SIGS_TRUE
            io.oMemByt     := MEM_BYT_X
            io.oMemAddrSrc := MEM_ADDR_SRC_X
            io.oGPRWrEn    := SIGS_FALSE
            io.oGPRWrSrc   := GPR_WR_SRC_X
            io.oPCWrEn     := SIGS_FALSE
            io.oPCWrConEn  := SIGS_FALSE
            io.oPCWrSrc    := PC_WR_SRC_X
        }
        is (STATE_IF) {
            rStateCurr := STATE_ID

            io.oIRWrEn := SIGS_TRUE
            io.oMemByt := MEM_BYT_4_U
            io.oPCWrEn := SIGS_FALSE
        }
        is (STATE_ID) {
            switch (wInstName) {
                is (INST_NAME_ADD) {
                    rStateCurr  := STATE_EX

                    io.oIRWrEn  := SIGS_FALSE
                    io.oALUType := ALU_TYPE_ADD
                    io.oALURS1  := ALU_RS1_GPR
                    io.oALURS2  := ALU_RS2_GPR
                }
            }
        }
        is (STATE_EX) {
            rStateCurr := STATE_WB
        }
        is (STATE_LS) {
        }
        is (STATE_WB) {
            rStateCurr   := STATE_IF

            io.oGPRWrEn  := SIGS_TRUE
            io.oGPRWrSrc := GPR_WR_SRC_ALU
            io.oPCWrEn   := SIGS_TRUE
            io.oPCWrSrc  := PC_WR_SRC_NPC
        }
    }
}
