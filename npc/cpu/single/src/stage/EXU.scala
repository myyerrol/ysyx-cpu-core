package cpu.stage

import chisel3._
import chisel3.util._

import cpu.comp._
import cpu.util.Base._

class EXU extends Module {
    val io = IO(new Bundle {
        val iInstRS1Addr = Input(UInt(REG_WIDTH.W))
        val iInstRS2Addr = Input(UInt(REG_WIDTH.W))
        val iInstRDAddr  = Input(UInt(REG_WIDTH.W))
        val iInstCSRAddr = Input(UInt(DATA_WIDTH.W))
        val iInstRS1Val  = Input(UInt(DATA_WIDTH.W))
        val iInstRS2Val  = Input(UInt(DATA_WIDTH.W))
        val iInstCSRVal  = Input(UInt(DATA_WIDTH.W))
        val iPC          = Input(UInt(DATA_WIDTH.W))
        val iMemRdData   = Input(UInt(DATA_WIDTH.W))

        val iInstName    = Input(UInt(SIGNAL_WIDTH.W))
        val iALUType     = Input(UInt(SIGNAL_WIDTH.W))
        val iALURS1Val   = Input(UInt(DATA_WIDTH.W))
        val iALURS2Val   = Input(UInt(DATA_WIDTH.W))
        val iJmpEn       = Input(Bool())
        val iMemWrEn     = Input(Bool())
        val iMemByt      = Input(UInt(SIGNAL_WIDTH.W))
        val iRegWrEn     = Input(Bool())
        var iRegWrSrc    = Input(UInt(DATA_WIDTH.W))

        val oALUOut        = Output(UInt(DATA_WIDTH.W))
        val oJmpEn         = Output(Bool())
        val oJmpPC         = Output(UInt(DATA_WIDTH.W))
        val oMemRdAddr     = Output(UInt(DATA_WIDTH.W))
        val oMemWrEn       = Output(Bool())
        val oMemWrAddr     = Output(UInt(DATA_WIDTH.W))
        val oMemWrData     = Output(UInt(DATA_WIDTH.W))
        val oMemWrLen      = Output(UInt(BYTE_WIDTH.W))
        val oRegWrEn       = Output(Bool())
        val oRegWrAddr     = Output(UInt(DATA_WIDTH.W))
        val oRegWrData     = Output(UInt(DATA_WIDTH.W))
        val oCSRWrEn       = Output(Bool())
        val oCSRWrMEn      = Output(Bool())
        val oCSRWrAddr     = Output(UInt(DATA_WIDTH.W))
        val oCSRWrMEPCAddr = Output(UInt(DATA_WIDTH.W))
        val oCSRWrMCAUAddr = Output(UInt(DATA_WIDTH.W))
        val oCSRWrData     = Output(UInt(DATA_WIDTH.W))
        val oCSRWrMEPCData = Output(UInt(DATA_WIDTH.W))
        val oCSRWrMCAUData = Output(UInt(DATA_WIDTH.W))
    })

    // 处理算术操作
    val jalrMask = Cat(Fill(DATA_WIDTH - 1, 1.U(1.W)), 0.U(1.U))
    val aluOut = MuxCase(
        0.U(DATA_WIDTH.W),
        Seq(
            (io.iALUType === ALU_TYPE_ADD)   ->  (io.iALURS1Val + io.iALURS2Val),
            (io.iALUType === ALU_TYPE_SUB)   ->  (io.iALURS1Val - io.iALURS2Val),
            (io.iALUType === ALU_TYPE_AND)   ->  (io.iALURS1Val & io.iALURS2Val),
            (io.iALUType === ALU_TYPE_OR)    ->  (io.iALURS1Val | io.iALURS2Val),
            (io.iALUType === ALU_TYPE_XOR)   ->  (io.iALURS1Val ^ io.iALURS2Val),
            (io.iALUType === ALU_TYPE_SLT)   ->  (io.iALURS1Val.asSInt() < io.iALURS2Val.asSInt()).asUInt(),
            (io.iALUType === ALU_TYPE_SLTU)  ->  (io.iALURS1Val.asUInt() < io.iALURS2Val.asUInt()).asUInt(),
            (io.iALUType === ALU_TYPE_SLL)   ->  (io.iALURS1Val << io.iALURS2Val(5, 0)),
            (io.iALUType === ALU_TYPE_SLLW)  ->  (io.iALURS1Val(31, 0) << io.iALURS2Val(4, 0)),
            (io.iALUType === ALU_TYPE_SRL)   ->  (io.iALURS1Val >> io.iALURS2Val(5, 0)),
            (io.iALUType === ALU_TYPE_SRLW)  ->  (io.iALURS1Val(31, 0) >> io.iALURS2Val(4, 0)),
            (io.iALUType === ALU_TYPE_SRLIW) ->  (io.iALURS1Val(31, 0) >> io.iALURS2Val(5, 0)),
            (io.iALUType === ALU_TYPE_SRA)   ->  (io.iALURS1Val.asSInt() >> io.iALURS2Val(5, 0).asUInt()).asUInt(),
            (io.iALUType === ALU_TYPE_SRAW)  ->  (io.iALURS1Val(31, 0).asSInt() >> io.iALURS2Val(4, 0).asUInt()).asUInt(),
            (io.iALUType === ALU_TYPE_SRAIW) ->  (io.iALURS1Val(31, 0).asSInt() >> io.iALURS2Val(5, 0).asUInt()).asUInt(),
            (io.iALUType === ALU_TYPE_BEQ)   ->  (io.iALURS1Val === io.iALURS2Val),
            (io.iALUType === ALU_TYPE_BNE)   ->  (io.iALURS1Val =/= io.iALURS2Val),
            (io.iALUType === ALU_TYPE_BLT)   ->  (io.iALURS1Val.asSInt() < io.iALURS2Val.asSInt()).asUInt(),
            (io.iALUType === ALU_TYPE_BGE)   ->  (io.iALURS1Val.asSInt() >= io.iALURS2Val.asSInt()).asUInt(),
            (io.iALUType === ALU_TYPE_BLTU)  ->  (io.iALURS1Val < io.iALURS2Val),
            (io.iALUType === ALU_TYPE_BGEU)  ->  (io.iALURS1Val >= io.iALURS2Val),
            (io.iALUType === ALU_TYPE_JALR)  -> ((io.iALURS1Val + io.iALURS2Val) & jalrMask),
            (io.iALUType === ALU_TYPE_MUL)   ->  (io.iALURS1Val * io.iALURS2Val),
            (io.iALUType === ALU_TYPE_DIVU)  ->  (io.iALURS1Val / io.iALURS2Val),
            (io.iALUType === ALU_TYPE_DIVW)  ->  (io.iALURS1Val(31, 0).asSInt() / io.iALURS2Val(31, 0).asSInt()).asUInt(),
            (io.iALUType === ALU_TYPE_DIVUW) ->  (io.iALURS1Val(31, 0).asUInt() / io.iALURS2Val(31, 0).asUInt()).asUInt(),
            (io.iALUType === ALU_TYPE_REMU)  ->  (io.iALURS1Val % io.iALURS2Val),
            (io.iALUType === ALU_TYPE_REMW)  ->  (io.iALURS1Val(31, 0).asSInt() % io.iALURS2Val(31, 0).asSInt()).asUInt()
        )
    )

    io.oJmpEn := false.B
    io.oJmpPC := io.iPC
    // 处理分支跳转操作
    when ((io.iInstName === INST_NAME_BEQ   ||
           io.iInstName === INST_NAME_BNE   ||
           io.iInstName === INST_NAME_BLT   ||
           io.iInstName === INST_NAME_BGE   ||
           io.iInstName === INST_NAME_BLTU  ||
           io.iInstName === INST_NAME_BGEU) &&
           aluOut === 1.U) {
        io.oJmpEn := true.B
        io.oJmpPC := io.iPC + io.iInstRS2Val
    }
    // 处理异常跳转操作
    when ((io.iInstName === INST_NAME_ECALL)) {
        io.oJmpEn := true.B
        io.oJmpPC := io.iInstCSRVal
    }
    // 处理正常跳转操作
    when (io.iJmpEn === true.B) {
        io.oJmpEn := true.B
        io.oJmpPC := aluOut
    }

    // 处理访存写入操作
    val memWrData = io.iInstRS2Val
    when (io.iMemWrEn) {
        io.oMemWrEn   := true.B
        io.oMemWrAddr := aluOut
        io.oMemWrData := memWrData
        io.oMemWrLen  := MuxCase(
            8.U(BYTE_WIDTH.W),
            Seq(
                (io.iMemByt === MEM_BYT_1_U) -> 1.U(BYTE_WIDTH.W),
                (io.iMemByt === MEM_BYT_2_U) -> 2.U(BYTE_WIDTH.W),
                (io.iMemByt === MEM_BYT_4_U) -> 4.U(BYTE_WIDTH.W),
                (io.iMemByt === MEM_BYT_8_U) -> 8.U(BYTE_WIDTH.W),
            )
        )
    }.otherwise {
        io.oMemWrEn   := false.B
        io.oMemWrAddr := 0.U(DATA_WIDTH.W)
        io.oMemWrData := 0.U(DATA_WIDTH.W)
        io.oMemWrLen  := 0.U(BYTE_WIDTH.W)
    }
    // 处理访存读取操作
    io.oMemRdAddr := 0.U(DATA_WIDTH.W)

    // 处理写回操作
    val aluOutByt4 = aluOut(31, 0)
    val aluOutData = MuxCase(
        aluOut,
        Seq(
            (io.iInstName === INST_NAME_ADDW  ||
             io.iInstName === INST_NAME_ADDIW ||
             io.iInstName === INST_NAME_SUBW  ||
             io.iInstName === INST_NAME_SLLW  ||
             io.iInstName === INST_NAME_SLLIW ||
             io.iInstName === INST_NAME_SRLW  ||
             io.iInstName === INST_NAME_SRLIW ||
             io.iInstName === INST_NAME_SRAW  ||
             io.iInstName === INST_NAME_SRAIW ||
             io.iInstName === INST_NAME_MULW  ||
             io.iInstName === INST_NAME_DIVW  ||
             io.iInstName === INST_NAME_DIVUW ||
             io.iInstName === INST_NAME_REMW) -> Cat(Fill(32, aluOutByt4(31)), aluOutByt4)
        )
    )
    val regWrData = MuxCase(
        0.U(DATA_WIDTH.W),
        Seq(
            (io.iRegWrSrc === REG_WR_SRC_ALU) -> aluOutData,
            (io.iRegWrSrc === REG_WR_SRC_PC)  -> (io.iPC + 4.U(DATA_WIDTH.W)),
            (io.iRegWrSrc === REG_WR_SRC_CSR) -> io.iInstCSRVal
        )
    )
    // 处理GPR写回操作
    when (io.iRegWrEn) {
        io.oRegWrEn   := true.B
        io.oRegWrAddr := io.iInstRDAddr
        when (io.iRegWrSrc === REG_WR_SRC_MEM) {
            io.oMemRdAddr := aluOut
            val iMemRdData = io.iMemRdData
            val memRdDataByt1 = iMemRdData(7, 0)
            val memRdDataByt2 = iMemRdData(15, 0)
            val memRdDataByt4 = iMemRdData(31, 0)
            val memRdDataByt8 = iMemRdData(63, 0)
            val memRdData = MuxCase(
                0.U(DATA_WIDTH.W),
                Seq(
                    (io.iMemByt === MEM_BYT_1_S) -> Cat(Fill(56, memRdDataByt1(7)),  memRdDataByt1),
                    (io.iMemByt === MEM_BYT_2_S) -> Cat(Fill(48, memRdDataByt2(15)), memRdDataByt2),
                    (io.iMemByt === MEM_BYT_1_U) -> Cat(Fill(56, 0.U), memRdDataByt1),
                    (io.iMemByt === MEM_BYT_2_U) -> Cat(Fill(48, 0.U), memRdDataByt2),
                    (io.iMemByt === MEM_BYT_4_S) -> Cat(Fill(32, memRdDataByt4(31)), memRdDataByt4),
                    (io.iMemByt === MEM_BYT_4_U) -> Cat(Fill(32, 0.U), memRdDataByt4),
                    (io.iMemByt === MEM_BYT_8_S) -> memRdDataByt8
                )
            )
            io.oRegWrData := memRdData
        }.otherwise {
            io.oRegWrData := regWrData
        }
    }.otherwise {
        io.oRegWrEn   := false.B
        io.oRegWrAddr := 0.U(DATA_WIDTH.W)
        io.oRegWrData := 0.U(DATA_WIDTH.W)
    }
    // 处理CSR写回操作
    when (io.iRegWrEn === true.B &&
          io.iRegWrSrc === REG_WR_SRC_CSR) {
        io.oCSRWrEn   := true.B
        io.oCSRWrAddr := io.iInstCSRAddr
        io.oCSRWrData := aluOutData
    }.otherwise {
        io.oCSRWrEn   := false.B
        io.oCSRWrAddr := 0.U(DATA_WIDTH.W)
        io.oCSRWrData := 0.U(DATA_WIDTH.W)
    }
    when (io.iInstName === INST_NAME_ECALL) {
        io.oCSRWrMEn      := true.B
        io.oCSRWrMEPCAddr := CSR_MEPC
        io.oCSRWrMCAUAddr := CSR_MCAUSE
        io.oCSRWrMEPCData := io.iPC
        io.oCSRWrMCAUData := -1.S(DATA_WIDTH.W).asUInt()
    }.otherwise {
        io.oCSRWrMEn      := false.B
        io.oCSRWrMEPCAddr := 0.U(DATA_WIDTH.W)
        io.oCSRWrMCAUAddr := 0.U(DATA_WIDTH.W)
        io.oCSRWrMEPCData := 0.U(DATA_WIDTH.W)
        io.oCSRWrMCAUData := 0.U(DATA_WIDTH.W)
    }

    io.oALUOut := aluOut
}
