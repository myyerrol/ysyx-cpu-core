import chisel3._
import chisel3.util._

class Top extends Module {
    val io = IO(new Bundle {
        val i_inst         =  Input(UInt(32.W))
        val i_inst_rs1_val =  Input(UInt(64.W))
        val i_inst_rs2_val =  Input(UInt(64.W))
        val o_pc           = Output(UInt(64.W))
        val o_inst_rs1     = Output(UInt( 5.W))
        val o_inst_rs2     = Output(UInt( 5.W))
        val o_inst_rd      = Output(UInt( 5.W))
        val o_inst_rd_val  = Output(UInt(64.W))
    });

    val ifu = Module(new IFU())
    val pc = RegInit("x80000000".U(64.W))
    ifu.io.i_pc := pc
    io.o_pc := ifu.io.o_pc

    val idu = Module(new IDU())
    idu.io.i_inst := io.i_inst
    io.o_inst_rs1 := idu.io.o_inst_rs1
    io.o_inst_rs2 := idu.io.o_inst_rs2

    val exu = Module(new EXU())
    exu.io.i_inst_type    := idu.io.o_inst_type
    exu.io.i_inst_rs1_val := io.i_inst_rs1_val
    exu.io.i_inst_rs2_val := io.i_inst_rs2_val
    exu.io.i_inst_rd      := idu.io.o_inst_rd
    exu.io.i_inst_imm     := idu.io.o_inst_imm
    io.o_inst_rd          := exu.io.o_inst_rd
    io.o_inst_rd_val      := exu.io.o_inst_rd_val

    val status = exu.io.o_status
    when (status === 0.U) {
        pc := pc + 4.U
    }
}
