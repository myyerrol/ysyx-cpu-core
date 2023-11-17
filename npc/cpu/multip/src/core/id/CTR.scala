package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class CTRIO extends Bundle with ConfigIO {
    val oInstName  = Output(UInt(SIGS_WIDTH.W))
    val oStateCurr = Output(UInt(SIGS_WIDTH.W))
    val oPCWrEn    = Output(Bool())
    val oPCWrConEn = Output(Bool())
    val oPCWrSrc   = Output(UInt(SIGS_WIDTH.W))
    val oPCNextEn  = Output(Bool())
    val oMemWrEn   = Output(Bool())
    val oMemByt    = Output(UInt(SIGS_WIDTH.W))
    val oIRWrEn    = Output(Bool())
    val oGPRWrEn   = Output(Bool())
    val oGPRWrSrc  = Output(UInt(SIGS_WIDTH.W))
    val oALUType   = Output(UInt(SIGS_WIDTH.W))
    val oALURS1    = Output(UInt(SIGS_WIDTH.W))
    val oALURS2    = Output(UInt(SIGS_WIDTH.W))
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

    val stateIF :: stateID :: stateEX :: stateLS :: stateWB :: Nil = Enum(5)
    val rStateCurr = RegInit(stateIF)

    val wPCWrEn    = WireInit(EN_FALSE)
    val wPCWrConEn = WireInit(EN_FALSE)
    val wPCWrSrc   = WireInit(PC_WR_SRC_X)
    val wPCNextEn  = WireInit(EN_FALSE)
    val wMemWrEn   = WireInit(EN_FALSE)
    val wMemByt    = WireInit(MEM_BYT_X)
    val wIRWrEn    = WireInit(EN_FALSE)
    val wGPRWrEn   = WireInit(EN_FALSE)
    val wGPRWrSrc  = WireInit(GPR_WR_SRC_X)
    val wALUType   = WireInit(ALU_TYPE_X)
    val wALURS1    = WireInit(ALU_RS1_X)
    val wALURS2    = WireInit(ALU_RS2_X)

    switch (rStateCurr) {
        is (stateIF) {
            rStateCurr := stateID
            wPCNextEn  := EN_TRUE
            wIRWrEn    := EN_TRUE
            wALUType   := ALU_TYPE_ADD
            wALURS1    := ALU_RS1_PC
            wALURS2    := ALU_RS2_4
        }
        is (stateID) {
            rStateCurr := stateEX
            switch (wInstName) {
                is (INST_NAME_JAL) {
                    wPCNextEn  := EN_TRUE
                    wALUType   := ALU_TYPE_ADD
                    wALURS1    := ALU_RS1_PC
                    wALURS2    := ALU_RS2_IMM_J
                }
            }
        }
        is (stateEX) {
            rStateCurr := stateWB

            switch (wInstName) {
                is (INST_NAME_ADD) {
                    wALUType := ALU_TYPE_ADD
                    wALURS1  := ALU_RS1_GPR
                    wALURS2  := ALU_RS2_GPR
                }
                is (INST_NAME_ADDI) {
                    wALUType := ALU_TYPE_ADD
                    wALURS1  := ALU_RS1_GPR
                    wALURS2  := ALU_RS2_IMM_I
                }
                is (INST_NAME_AUIPC) {
                    wALUType := ALU_TYPE_ADD
                    wALURS1  := ALU_RS1_PC
                    wALURS2  := ALU_RS2_IMM_U
                }
                is (INST_NAME_JAL) {
                    wALUType := ALU_TYPE_ADD
                    wALURS1  := ALU_RS1_PC
                    wALURS2  := ALU_RS2_4
                }
                is (INST_NAME_SD) {
                    rStateCurr := stateLS
                    wALUType := ALU_TYPE_ADD
                    wALURS1  := ALU_RS1_GPR
                    wALURS2  := ALU_RS2_IMM_S
                }
                is (INST_NAME_JALR) {
                    rStateCurr := stateLS
                    wPCNextEn  := EN_TRUE
                    wALUType   := ALU_TYPE_JALR
                    wALURS1    := ALU_RS1_GPR
                    wALURS2    := ALU_RS2_IMM_I
                }
            }
        }
        is (stateLS) {
            switch (wInstName) {
                is (INST_NAME_SD) {
                    rStateCurr := stateIF
                    wPCWrEn    := EN_TRUE
                    wPCWrSrc   := PC_WR_SRC_NPC
                    wMemWrEn   := EN_TRUE
                    wMemByt    := MEM_BYT_8_U
                }
                // 临时
                is (INST_NAME_JALR) {
                    rStateCurr := stateWB
                    wALUType := ALU_TYPE_ADD
                    wALURS1  := ALU_RS1_PC
                    wALURS2  := ALU_RS2_4
                }
            }
        }
        is (stateWB) {
            rStateCurr := stateIF
            wPCWrEn    := EN_TRUE
            wPCWrSrc   := PC_WR_SRC_NPC
            wGPRWrEn   := EN_TRUE
            wGPRWrSrc  := GPR_WR_SRC_ALU
        }
    }

    io.ctrio.oInstName  := wInstName
    io.ctrio.oStateCurr := rStateCurr
    io.ctrio.oPCWrEn    := wPCWrEn
    io.ctrio.oPCWrConEn := wPCWrConEn
    io.ctrio.oPCWrSrc   := wPCWrSrc
    io.ctrio.oPCNextEn  := wPCNextEn
    io.ctrio.oMemWrEn   := wMemWrEn
    io.ctrio.oMemByt    := wMemByt
    io.ctrio.oIRWrEn    := wIRWrEn
    io.ctrio.oGPRWrEn   := wGPRWrEn
    io.ctrio.oGPRWrSrc  := wGPRWrSrc
    io.ctrio.oALUType   := wALUType
    io.ctrio.oALURS1    := wALURS1
    io.ctrio.oALURS2    := wALURS2
}
