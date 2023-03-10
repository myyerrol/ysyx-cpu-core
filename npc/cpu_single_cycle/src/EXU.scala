import chisel3._
import chisel3.util._

import utils.Base._

class EXU extends Module {
    val io = IO(new Bundle {
        val iInstType =  Input(UInt(32.W))
        val iInstRS1  =  Input(UInt( 5.W))
        val iInstRS2  =  Input(UInt( 5.W))
        val iInstRD   =  Input(UInt( 5.W))
        val iInstImm  =  Input(SInt(64.W))
        val oHalt     = Output(Bool())
    })

    val RegFile = Mem(32, UInt(64.W))
    def readReg(addr: UInt) = Mux(addr === 0.U, 0.U(64.W), RegFile(addr))

    val iInstRS1Val = readReg(iInstRS1)
    val iInstRS2Val = readReg(iInstRS2)

    io.oHalt := (io.iInstType === EBREAK.U) && (iInstRS1 === 1.U)

    switch (io.iInstType) {
        is (ADDI.U) {
            RegFile(io.iInstRD) := iInstRS1Val + io.iInstImm
        }
        is (EBREAK.U) {
            when (iInstRS1Val === 0.U) {
                printf("%c", iInstRS2Val(7,0))
            }
        }
    }
}
