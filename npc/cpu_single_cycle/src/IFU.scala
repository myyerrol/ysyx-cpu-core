import chisel3._
import chisel3.util._

class IFU extends Module {
    val io = IO(new Bundle {
        val i_pc =  Input(UInt(64.W))
        val o_pc = Output(UInt(64.W))
    })

    io.o_pc := io.i_pc
}
