import chisel3._
import chisel3.util._

class IFU extends Module {
    val io = IO(new Bundle {
        val iPC =  Input(UInt(64.W))
        val oPC = Output(UInt(64.W))
    })

    io.oPC := io.iPC
}
