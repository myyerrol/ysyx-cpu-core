package cpu.stage.pipeline

import chisel3._
import chisel3.util._

import cpu.blackbox._
import cpu.common._
import cpu.port._

class LSU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iMemRdEn   = Input(Bool())
        val iMemWrEn   = Input(Bool())
        val iMemByt    = Input(UInt(SIGS_WIDTH.W))

        val iPC        = Input(UInt(ADDR_WIDTH.W))
        val iALUOut    = Input(UInt(DATA_WIDTH.W))
        val iMemWrData = Input(UInt(DATA_WIDTH.W))

        val pLSU       = new LSUIO
    })

    io.pLSU.oMemRdEn       := io.iMemRdEn
    io.pLSU.oMemRdAddrInst := io.iPC
    io.pLSU.oMemRdAddrLoad := io.iALUOut

    when (io.iMemWrEn) {
        io.pLSU.oMemWrEn   := true.B
        io.pLSU.oMemWrAddr := io.iALUOut
        io.pLSU.oMemWrData := io.iMemWrData
        io.pLSU.oMemWrLen  := MuxLookup(
            io.iMemByt,
            8.U(BYTE_WIDTH.W),
            Seq(
                MEM_BYT_1_U -> 1.U(BYTE_WIDTH.W),
                MEM_BYT_2_U -> 2.U(BYTE_WIDTH.W),
                MEM_BYT_4_U -> 4.U(BYTE_WIDTH.W),
                MEM_BYT_8_U -> 8.U(BYTE_WIDTH.W),
            )
        )
    }
    .otherwise {
        io.pLSU.oMemWrEn   := false.B
        io.pLSU.oMemWrAddr := DATA_ZERO
        io.pLSU.oMemWrData := DATA_ZERO
        io.pLSU.oMemWrLen  := DATA_ZERO
    }

    io.pLSU.oMemRdDataInst := DontCare
    io.pLSU.oMemRdDataLoad := DontCare

    val wMemRdData = WireInit(DATA_ZERO)

    if (MEMS_TYP.equals("DPIDirect")) {
        val mMemDPIDirectComob = Module(new MemDPIDirectComb)
        mMemDPIDirectComob.io.iClock         := clock
        mMemDPIDirectComob.io.iReset         := reset
        mMemDPIDirectComob.io.iMemRdEn       := io.pLSU.oMemRdEn
        mMemDPIDirectComob.io.iMemRdAddrInst := io.pLSU.oMemRdAddrInst
        mMemDPIDirectComob.io.iMemRdAddrLoad := io.pLSU.oMemRdAddrLoad
        mMemDPIDirectComob.io.iMemWrEn       := io.pLSU.oMemWrEn
        mMemDPIDirectComob.io.iMemWrAddr     := io.pLSU.oMemWrAddr
        mMemDPIDirectComob.io.iMemWrData     := io.pLSU.oMemWrData
        mMemDPIDirectComob.io.iMemWrLen      := io.pLSU.oMemWrLen

        io.pLSU.oMemRdDataInst := mMemDPIDirectComob.io.oMemRdDataInst
        io.pLSU.oMemRdDataLoad := mMemDPIDirectComob.io.oMemRdDataLoad

        wMemRdData := mMemDPIDirectComob.io.oMemRdDataLoad
    }
    else {
        io.pLSU.oMemRdDataInst := DATA_ZERO
        io.pLSU.oMemRdDataLoad := DATA_ZERO

        wMemRdData := DATA_ZERO
    }

    io.pLSU.oMemRdData := wMemRdData
}
