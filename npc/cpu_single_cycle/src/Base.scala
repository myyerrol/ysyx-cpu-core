import chisel3._
import chisel3.util._

object Base {
    val ADDI   = 0x01
    val EBREAK = 0x02

    val PC     = 0x80000000
}
