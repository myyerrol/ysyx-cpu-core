package cpu.port

import chisel3._
import chisel3.util._

import cpu.common._

class ITraceIO extends Bundle with ConfigIO {
    val pCTR = new CTRIO
    val pIDU = new IDUIO
    val pEXU = new EXUIO
    val pLSU = new LSUIO
    val pWBU = new WBUIO
}
