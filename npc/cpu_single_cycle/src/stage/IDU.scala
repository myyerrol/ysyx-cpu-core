package stage

import chisel3._
import chisel3.util._
import chisel3.util.experimental.decode

import utils.Base._
import utils.Inst._

class IDU extends Module {
    val io = IO(new Bundle {
        val iInst    =  Input(UInt(DATA_WIDTH.W))
        val oInstRS1 = Output(UInt(5.W))
        val oInstRS2 = Output(UInt(5.W))
        val oInstRD  = Output(UInt(5.W))
        val oALUType = Output(UInt(5.W))
        val oALURS1  = Output(UInt(5.W))
        val oALURS2  = Output(UInt(5.W))
    })

    val inst = io.iInst;
    val aluType::aluRS1::aluRS2::memW::regW::crsW::Nil = signals
    val signals =  ListLookup(
        inst,
        List(ALU_TYPE_X, ALU_RS1_X, ALU_RS2_X, MEM_W_X, REG_W_X, CSR_W_X),
        Array(
            ADDI -> List(ALU_TYPE_ADD, ALU_RS1_R ALU_RS2_I, MEM_W_F, REG_W_T, CSR_W_F)
        )
    )

    val rs1 = inst(19, 15)
    val rs2 = inst(24, 20)
    val rd  = inst(11, 7)
    val immI = inst(31, 20)
    val immISext = Cat(Fill(52, immI(11)), immI)



    io.oInstRS1  := rs1
    io.oInstRS2  := rs2
    io.oInstRD   := rd
    io.oInstImm  := imm
    io.oALUType  := aluType
    io.oALURS1   := aluRS1
    io.oALURS2   := aluRS2




    // when (inst_type === INST_TYPE_R) {
    //     imm = 0.U
    // }.elsewhen (inst_type === INST_TYPE_I) {
    //     val immI = inst(31, 20)
    //     imm = Cat(Fill(52, immI(11)), immI)
    // }.elsewhen (inst_type === INST_TYPE_S) {
    //     val immS = Cat(inst(31, 25), inst(11, 7))
    //     imm = Cat(Fill(52, immS(11)), immS)
    // }.elsewhen (inst_type === INST_TYPE_B) {
    //     val immB = Cat(inst(31), inst(7), inst(30, 25), inst(11, 8)) << 1
    //     imm = Cat(Fill(52, immB(11)), immB)
    // }.elsewhen (inst_type === INST_TYPE_U) {
    //     val immU = Cat(inst(31, 12))
    //     imm = Cat(Fill(44, immU(19)), immU)
    // }.elsewhen (inst_type === INST_TYPE_J) {
    //     val immJ = Cat(inst(31), inst(19, 12), inst(20), inst(30, 21)) << 1
    //     imm = Cat(Fill(44, immJ(19)), immJ)
    // }.otherwise {
    //     assert(false.B, "Invalid instruction 0x%x", inst)
    // }

    // val io = IO(new Bundle {
    //     val iInst     =  Input(UInt(32.W))
    //     val oInstType = Output(UInt(32.W))
    //     val oInstRS1  = Output(UInt( 5.W))
    //     val oInstRS2  = Output(UInt( 5.W))
    //     val oInstRD   = Output(UInt( 5.W))
    //     val oInstImm  = Output(UInt( 64.W))
    // })

    // val InstTypeI = new Bundle {
    //     val imm11_0 = UInt(12.W)
    //     val rs1     = UInt( 5.W)
    //     val funct3  = UInt( 3.W)
    //     val rd      = UInt( 5.W)
    //     val opcode  = UInt( 7.W)
    // }
    // def extSign(imm11_0: UInt) = Cat(Fill(52, imm11_0(11)), imm11_0)

    // val inst = io.iInst.asTypeOf(InstTypeI)

    // when ((inst.opcode === "b0010011".U) && (inst.funct3 === "b000".U)) {
    //     io.oInstType := ADDI.U
    // }.elsewhen(inst.opcode === "b0010111") {
    //     io.oInstType := AUIPC.U
    // }.elsewhen (inst.asUInt === "x00100073".U) {
    //     io.oInstType := EBREAK.U
    // }.otherwise {
    //     io.oInstType := INV.U
    //     assert(false.B, "Invalid instruction 0x%x", inst.asUInt)
    // }

    // io.oInstRS1  := Mux((io.oInstType === EBREAK.U), 10.U(5.W), inst.rs1)
    // io.oInstRS2  := Mux((io.oInstType === EBREAK.U), 11.U(5.W), 0.U(5.W))
    // io.oInstRD   := inst.rd
    // io.oInstImm  := extSign(inst.imm11_0)
}
