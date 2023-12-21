package cpu.mem

import chisel3._
import chisel3.util._

import cpu.blackbox._
import cpu.common._
import cpu.port._

class AXI4LiteIFU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iAddr = Input (UInt(ADDR_WIDTH.W))
        val oData = Output(UInt(DATA_WIDTH.W))

        val pAXI4 = new AXI4LiteIO
    })

    val mAXI4LiteM = Module(new AXI4LiteM)
    mAXI4LiteM.io.iClock := clock;
    mAXI4LiteM.io.iReset := reset;
    mAXI4LiteM.io.iMode  := MODE_RD
    mAXI4LiteM.io.iAddr  := io.iAddr
    mAXI4LiteM.io.iData  := DontCare
    mAXI4LiteM.io.iMask  := DontCare

    io.oData := mAXI4LiteM.io.oData

    io.pAXI4 <> mAXI4LiteM.io.pAXI4
}

// class AXI4LiteLFU extends Module with ConfigInst {
//     val io = IO(new Bundle {
//         val iAddr = Input (UInt(ADDR_WIDTH.W))
//         val oData = Output(UInt(DATA_WIDTH.W))
//     })

//     val mAXI4LiteM = Module(new AXI4LiteM)
//     mAXI4LiteM.io.iClock := clock;
//     mAXI4LiteM.io.iReset := reset;
//     mAXI4LiteM.io.iMode  := MODE_RD
//     mAXI4LiteM.io.iAddr  := io.iAddr
//     mAXI4LiteM.io.iData  := DontCare
//     mAXI4LiteM.io.iMask  := DontCare

//     val mAXI4LiteS = Module(new AXI4LiteS)
//     mAXI4LiteS.io.iClock := clock
//     mAXI4LiteS.io.iReset := reset
//     mAXI4LiteS.io.iMode  := MODE_RD
//     mAXI4LiteS.io.iData  := mMemDPIDirect.io.oMemRdDataInst
//     mAXI4LiteS.io.iResp  := RESP_OKEY
//     mAXI4LiteS.io.oData  := DontCare
//     mAXI4LiteS.io.oMask  := DontCare

//     val mMemDPIDirect = Module(new MemDPIDirect)
//     mMemDPIDirect.io.iClock         := clock
//     mMemDPIDirect.io.iReset         := reset
//     mMemDPIDirect.io.iMemRdEn       := true.B
//     mMemDPIDirect.io.iMemRdAddrInst := mAXI4LiteS.io.oAddr
//     mMemDPIDirect.io.iMemRdAddrLoad := DontCare
//     mMemDPIDirect.io.iMemWrEn       := false.B
//     mMemDPIDirect.io.iMemWrAddr     := DontCare
//     mMemDPIDirect.io.iMemWrData     := DontCare
//     mMemDPIDirect.io.iMemWrLen      := DontCare

//     mAXI4LiteM.io.pAXI4 <> mAXI4LiteS.io.pAXI4

//     io.oData := mAXI4LiteM.io.oData
// }
