package cpu.stage

import chisel3._
import chisel3.util._

import cpu.util.Base._

class EXU extends Module {
    val io = IO(new Bundle {
        val iInstRS1Addr = Input(UInt(5.W))
        val iInstRS2Addr = Input(UInt(5.W))
        val iInstRDAddr  = Input(UInt(5.W))
        val iALUType     = Input(UInt(5.W))
        val iALURS1Val   = Input(UInt(DATA_WIDTH.W))
        val iALURS2Val   = Input(UInt(DATA_WIDTH.W))
        val iMemWrEn     = Input(Bool())
        val iRegWrEn     = Input(Bool())
        val iCsrWrEn     = Input(Bool())

        val oRegWrEn     = Input(Bool())
        val oRegWrAddr   = Input(UInt(DATA_WIDTH.W))
        val oRegWrData   = Input(UInt(DATA_WIDTH.W))
    })

    val aluOut = MuxCase(
        0.U(DATA_WIDTH.W),
        Seq(
            (io.iALUType === ALU_TYPE_ADD) -> (io.iALURS1Val + io.iALURS2Val)
        )
    )

    when (io.iRegWrEn) {
        io.oRegWrEn   := true.B
        io.oRegWrAddr := io.iInstRDAddr
        io.oRegWrData := aluOut
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
