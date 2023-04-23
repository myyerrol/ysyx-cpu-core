package cpu.stage

import chisel3._
import chisel3.util._

import cpu.util.Base._

class EXU extends Module {
    val io = IO(new Bundle {
        val iInstRS1Addr = Input(UInt(REG_WIDTH.W))
        val iInstRS2Addr = Input(UInt(REG_WIDTH.W))
        val iInstRDAddr  = Input(UInt(REG_WIDTH.W))
        val iInstRS1Val  = Input(UInt(DATA_WIDTH.W))
        val iInstRS2Val  = Input(UInt(DATA_WIDTH.W))
        val iPC          = Input(UInt(DATA_WIDTH.W))
        val iALUType     = Input(UInt(SIGNAL_WIDTH.W))
        val iALURS1Val   = Input(UInt(DATA_WIDTH.W))
        val iALURS2Val   = Input(UInt(DATA_WIDTH.W))
        val iJmpEn       = Input(Bool())
        val iMemWrEn     = Input(Bool())
        val iMemByt      = Input(UInt(SIGNAL_WIDTH.W))
        val iRegWrEn     = Input(Bool())
        var iRegWrSrc    = Input(UInt(DATA_WIDTH.W))

        val oALUOut      = Output(UInt(DATA_WIDTH.W))
        val oJmpEn       = Output(Bool())
        val oJmpPC       = Output(UInt(DATA_WIDTH.W))
        val oMemWrEn     = Output(Bool())
        val oMemWrAddr   = Output(UInt(DATA_WIDTH.W))
        val oMemWrData   = Output(UInt(DATA_WIDTH.W))
        val oRegWrEn     = Output(Bool())
        val oRegWrAddr   = Output(UInt(DATA_WIDTH.W))
        val oRegWrData   = Output(UInt(DATA_WIDTH.W))
    })

    val jalrMask = Cat(Fill(DATA_WIDTH - 1, 1.U(1.W)), 0.U(1.U))
    val aluOut = MuxCase(
        0.U(DATA_WIDTH.W),
        Seq(
            (io.iALUType === ALU_TYPE_ADD)  ->  (io.iALURS1Val + io.iALURS2Val),
            (io.iALUType === ALU_TYPE_JALR) -> ((io.iALURS1Val + io.iALURS2Val) & jalrMask)
        )
    )

    io.oALUOut := aluOut

    when (io.iJmpEn === true.B) {
        io.oJmpEn := true.B
        io.oJmpPC := aluOut
    }.otherwise {
        io.oJmpEn := false.B
        io.oJmpPC := io.iPC
    }

    val memWrData = io.iInstRS2Val
    when (io.iMemWrEn) {
        io.oMemWrEn   := true.B
        io.oMemWrAddr := MuxCase(
            aluOut,
            Seq(
                (io.iMemByt === MEM_BYT_1) -> aluOut(7, 0),
                (io.iMemByt === MEM_BYT_2) -> aluOut(15, 0),
                (io.iMemByt === MEM_BYT_4) -> aluOut(31, 0),
                (io.iMemByt === MEM_BYT_8) -> aluOut(63, 0)
            )
        )
        io.oMemWrData := memWrData
    }.otherwise {
        io.oMemWrEn   := false.B
        io.oMemWrAddr := 0.U(DATA_WIDTH.W)
        io.oMemWrData := 0.U(DATA_WIDTH.W)
    }

    val regWrData = MuxCase(
        0.U(DATA_WIDTH.W),
        Seq(
            (io.iRegWrSrc === REG_WR_SRC_ALU) -> aluOut,
            (io.iRegWrSrc === REG_WR_SRC_PC)  -> (io.iPC + 4.U(DATA_WIDTH.W))
        )
    )
    when (io.iRegWrEn) {
        io.oRegWrEn   := true.B
        io.oRegWrAddr := io.iInstRDAddr
        io.oRegWrData := regWrData
    }.otherwise {
        io.oRegWrEn   := false.B
        io.oRegWrAddr := 0.U(DATA_WIDTH.W)
        io.oRegWrData := 0.U(DATA_WIDTH.W)
    }
}
