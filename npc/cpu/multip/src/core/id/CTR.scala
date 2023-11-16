package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class CTRIO extends Bundle with ConfigIO {
    val oInstName   = Output(UInt(SIGS_WIDTH.W))

    val oPCWrEn     = Output(UInt(SIGS_WIDTH.W))
    val oPCWrConEn  = Output(UInt(SIGS_WIDTH.W))
    val oPCWrSrc    = Output(UInt(SIGS_WIDTH.W))
    val oMemWrEn    = Output(UInt(SIGS_WIDTH.W))
    val oMemRdEn    = Output(UInt(SIGS_WIDTH.W))
    val oMemByt     = Output(UInt(SIGS_WIDTH.W))
    val oMemAddrSrc = Output(UInt(SIGS_WIDTH.W))
    val oIRWrEn     = Output(UInt(SIGS_WIDTH.W))
    val oGPRWrEn    = Output(UInt(SIGS_WIDTH.W))
    val oGPRWrSrc   = Output(UInt(SIGS_WIDTH.W))
    val oALUType    = Output(UInt(SIGS_WIDTH.W))
    val oALURS1     = Output(UInt(SIGS_WIDTH.W))
    val oALURS2     = Output(UInt(SIGS_WIDTH.W))
}

class CTR extends Module with ConfigInstPattern {
    val io = IO(new Bundle {
        val iPC   = Input(UInt(DATA_WIDTH.W))
        val iInst = Input(UInt(DATA_WIDTH.W))
        val ctrio = new CTRIO
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

    io.ctrio.oInstName := wInstName

    val stateRS :: stateIF :: stateID :: stateEX :: stateLS :: stateWB :: Nil = Enum(6)
    val rStateCurr = RegInit(stateRS)

    switch (rStateCurr) {
        is (stateRS) {
            rStateCurr := stateIF

            io.ctrio.oPCWrEn     := SIG_FALSE
            io.ctrio.oPCWrConEn  := SIG_FALSE
            io.ctrio.oPCWrSrc    := PC_WR_SRC_X
            io.ctrio.oMemWrEn    := SIG_FALSE
            io.ctrio.oMemRdEn    := SIG_TRUE
            io.ctrio.oMemByt     := MEM_BYT_X
            io.ctrio.oMemAddrSrc := MEM_ADDR_SRC_X
            io.ctrio.oIRWrEn     := SIG_FALSE
            io.ctrio.oGPRWrEn    := SIG_FALSE
            io.ctrio.oGPRWrSrc   := GPR_WR_SRC_X
            io.ctrio.oALUType    := ALU_TYPE_X
            io.ctrio.oALURS1     := ALU_RS1_X
            io.ctrio.oALURS2     := ALU_RS2_X
        }
        is (stateIF) {
            rStateCurr := stateID

            io.ctrio.oPCWrEn := SIG_FALSE
            io.ctrio.oMemByt := MEM_BYT_4_U
            io.ctrio.oIRWrEn := SIG_TRUE
        }
        is (stateID) {
            switch (wInstName) {
                is (INST_NAME_ADD) {
                    rStateCurr := stateEX

                    io.ctrio.oIRWrEn  := SIG_FALSE
                    io.ctrio.oALUType := ALU_TYPE_ADD
                    io.ctrio.oALURS1  := ALU_RS1_GPR
                    io.ctrio.oALURS2  := ALU_RS2_GPR
                }
            }
        }
        is (stateEX) {
            rStateCurr := stateWB
        }
        is (stateLS) {
        }
        is (stateWB) {
            rStateCurr := stateIF

            io.ctrio.oPCWrEn   := SIG_TRUE
            io.ctrio.oPCWrSrc  := PC_WR_SRC_NPC
            io.ctrio.oGPRWrEn  := SIG_TRUE
            io.ctrio.oGPRWrSrc := GPR_WR_SRC_ALU
        }
    }
}
