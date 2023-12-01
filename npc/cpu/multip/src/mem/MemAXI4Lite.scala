package cpu.mem

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.core._

class SRAMIFU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val bIFUAXISlaveARIO = Flipped(new IFUAXI4LiteARIO)
        // val bIFUAXISlaveAIO  = Flipped(new IFUAXI4LiteRIO)
    })

    // val stateMasterIDLE :: stateMasterWait :: Nil = Enum(2)
    // val stateMaster = RegInit(stateMasterIDLE)
}

class SRAMLSU extends Module with ConfigInst {

}

