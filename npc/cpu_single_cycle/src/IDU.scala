import chisel3._
import chisel3.util._

import utils.Base._

class IDU extends Module {
    val io = IO(new Bundle {
        val iInst     =  Input(UInt(32.W))
        val oInstType = Output(UInt(32.W))
        val oInstRS1  = Output(UInt( 5.W))
        val oInstRS2  = Output(UInt( 5.W))
        val oInstRD   = Output(UInt( 5.W))
        val oInstImm  = Output(UInt( 64.W))
    })

    val InstTypeI = new Bundle {
        val imm11_0 = UInt(12.W)
        val rs1     = UInt( 5.W)
        val funct3  = UInt( 3.W)
        val rd      = UInt( 5.W)
        val opcode  = UInt( 7.W)
    }
    def extSign(imm11_0: UInt) = Cat(Fill(52, imm11_0(11)), imm11_0)

    val inst = io.iInst.asTypeOf(InstTypeI)

    when ((inst.opcode === "b0010011".U) && (inst.funct3 === "b000".U)) {
        io.oInstType := ADDI.U
    }.elsewhen (inst.asUInt === "x00100073".U) {
        io.oInstType := EBREAK.U
    }.otherwise {
        io.oInstType := EBREAK.U
        assert(false.B, "Invalid instruction 0x%x", inst.asUInt)
    }

    io.oInstRS1  := Mux((io.oInstType === EBREAK.U), 10.U(5.W), inst.rs1)
    io.oInstRS2  := Mux((io.oInstType === EBREAK.U), 11.U(5.W), 0.U(5.W))
    io.oInstRD   := inst.rd
    io.oInstImm  := extSign(inst.imm11_0)
}
