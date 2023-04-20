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
        val iRegWrEn     = Input(Bool())
        var iRegWrSrc    = Input(UInt(DATA_WIDTH.W))
        val iCsrWrEn     = Input(Bool())

        val oALUOut      = Output(UInt(DATA_WIDTH.W))
        val oJmpEn       = Output(Bool())
        val oPC          = Output(UInt(DATA_WIDTH.W))
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
        io.oPC := aluOut
    }.otherwise {
        io.oJmpEn := false.B
        io.oPC := io.iPC
    }

    val memWrData = io.iInstRS2Val
    when (io.iMemWrEn) {
        io.oMemWrEn   := true.B
        io.oMemWrAddr := aluOut
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

    // val io = IO(new Bundle {
    //     val iInstType  =  Input(UInt(32.W))
    //     val iInstRS1   =  Input(UInt( 5.W))
    //     val iInstRS2   =  Input(UInt( 5.W))
    //     val iInstRD    =  Input(UInt( 5.W))
    //     val iInstImm   =  Input(UInt(64.W))
    //     val oInstRDVal = Output(UInt(64.W))
    //     val oHalt      = Output(Bool())
    // })

    // val regFile = Mem(32, UInt(64.W))
    // def readReg(addr: UInt) = Mux(addr === 0.U, 0.U(64.W), regFile(addr))

    // val iInstType   = io.iInstType
    // val iInstRS1Val = readReg(io.iInstRS1)
    // val iInstRS2Val = readReg(io.iInstRS2)

    // io.oInstRDVal := 0.U
    // io.oHalt := (iInstType === EBREAK.U) && (iInstRS1Val === 1.U)

    // switch (iInstType) {
    //     is (ADDI.U) {
    //         io.oInstRDVal := iInstRS1Val + io.iInstImm
    //         regFile(io.iInstRD) := io.oInstRDVal
    //     }
    //     is (EBREAK.U) {
    //         when (iInstRS1Val === 0.U) {
    //             printf("%c", iInstRS2Val(7, 0))
    //         }
    //     }
    // }
}
