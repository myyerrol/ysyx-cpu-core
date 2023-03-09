import chisel3._
import chisel3.util._

import Base._

class EXU extends Module {
    val io = IO(new Bundle {
        val i_inst_type     =  Input(UInt(32.W))
        val i_inst_rs1_val  =  Input(UInt(64.W))
        val i_inst_rs2_val  =  Input(UInt(64.W))
        val i_inst_rd       =  Input(UInt( 5.W))
        val i_inst_imm      =  Input(UInt(64.W))
        val o_inst_rd       = Output(UInt( 5.W))
        val o_inst_rd_val   = Output(UInt(64.W))
        val o_status        = Output(UInt(32.W))
    })

    io.o_inst_rd := io.i_inst_rd
    io.o_inst_rd_val := 0.U
    io.o_status := 0.U

    switch (io.i_inst_type) {
        is (ADDI.U) {
            io.o_inst_rd_val := io.i_inst_rs1_val + io.i_inst_imm
        }
        is (EBREAK.U) {
            io.o_inst_rd_val := 1.U
            io.o_status := 1.U
        }
    }
}
