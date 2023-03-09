import chisel3._
import chisel3.util._

import Base._

class IDU extends Module {
    val io = IO(new Bundle {
        val i_inst      =  Input(UInt(32.W))
        val o_inst_type = Output(UInt(32.W))
        val o_inst_rs1  = Output(UInt( 5.W))
        val o_inst_rs2  = Output(UInt( 5.W))
        val o_inst_rd   = Output(UInt( 5.W))
        val o_inst_imm  = Output(UInt( 64.W))
    })

    val InstTypeI = new Bundle {
        val imm11_0 = UInt(12.W)
        val rs1     = UInt( 5.W)
        val funct3  = UInt( 3.W)
        val rd      = UInt( 5.W)
        val opcode  = UInt( 7.W)
    }
    def SignEXT(imm11_0: UInt) = Cat(Fill(52, imm11_0(11)), imm11_0)

    val inst = io.i_inst.asTypeOf(InstTypeI)
    val isAddi = (inst.opcode === "b0010011".U) && (inst.funct3 === "b000".U)
    val isEbreak = inst.asUInt === "x00100073".U
    assert(isAddi || isEbreak, "Invalid instruction 0x%x", inst.asUInt)

    when (isAddi) {
        io.o_inst_type := ADDI.U
    }.elsewhen (isEbreak) {
        io.o_inst_type := EBREAK.U
    }.otherwise {
        io.o_inst_type := EBREAK.U
    }

    io.o_inst_rs1  := Mux(isEbreak, 10.U(5.W), inst.rs1)
    io.o_inst_rs2  := Mux(isEbreak, 11.U(5.W), 0.U(5.W))
    io.o_inst_rd   := inst.rd
    io.o_inst_imm  := SignEXT(inst.imm11_0)
}
