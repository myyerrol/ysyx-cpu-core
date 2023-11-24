package cpu.common

import chisel3._
import chisel3.util._

object SignExten {
    def apply(num: UInt, len: Int) = {
        val numLen  = num.getWidth
        val signBit = num(numLen - 1)
        if (numLen >= len) {
            num(len - 1, 0)
        }
        else {
            Cat(Fill(len - numLen, signBit), num)
        }
    }
}

object ZeroExten {
    def apply(num: UInt, len: Int) = {
        val numLen  = num.getWidth
        if (numLen >= len) {
            num(len - 1, 0)
        }
        else {
            Cat(0.U((len - numLen).W), num)
        }
    }
}
