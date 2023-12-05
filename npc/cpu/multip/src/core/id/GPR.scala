package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._
import cpu.port._

class GPR extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iWrEn      = Input (Bool())
        val iRd1Addr   = Input (UInt(DATA_WIDTH.W))
        val iRd2Addr   = Input (UInt(DATA_WIDTH.W))
        val iWrAddr    = Input (UInt(DATA_WIDTH.W))
        val iWrData    = Input (UInt(DATA_WIDTH.W))

        val oRd1Data   = Output(UInt(DATA_WIDTH.W))
        val oRd2Data   = Output(UInt(DATA_WIDTH.W))
        val oRdEndData = Output(UInt(DATA_WIDTH.W))

        val pGPR       = new GPRIO
    })

    val mGPR = Mem(GPRS_NUM, UInt(DATA_WIDTH.W))
    mGPR(0.U) := 0.U(DATA_WIDTH.W)

    when (io.iWrEn) {
        mGPR(io.iWrAddr) := io.iWrData
    }

    io.oRd1Data   := mGPR(io.iRd1Addr)
    io.oRd2Data   := mGPR(io.iRd2Addr)
    io.oRdEndData := mGPR(GPRS_10)

    io.pGPR.oRdData0  := mGPR( 0.U(GPRS_WIDTH.W))
    io.pGPR.oRdData1  := mGPR( 1.U(GPRS_WIDTH.W))
    io.pGPR.oRdData2  := mGPR( 2.U(GPRS_WIDTH.W))
    io.pGPR.oRdData3  := mGPR( 3.U(GPRS_WIDTH.W))
    io.pGPR.oRdData4  := mGPR( 4.U(GPRS_WIDTH.W))
    io.pGPR.oRdData5  := mGPR( 5.U(GPRS_WIDTH.W))
    io.pGPR.oRdData6  := mGPR( 6.U(GPRS_WIDTH.W))
    io.pGPR.oRdData7  := mGPR( 7.U(GPRS_WIDTH.W))
    io.pGPR.oRdData8  := mGPR( 8.U(GPRS_WIDTH.W))
    io.pGPR.oRdData9  := mGPR( 9.U(GPRS_WIDTH.W))
    io.pGPR.oRdData10 := mGPR(10.U(GPRS_WIDTH.W))
    io.pGPR.oRdData11 := mGPR(11.U(GPRS_WIDTH.W))
    io.pGPR.oRdData12 := mGPR(12.U(GPRS_WIDTH.W))
    io.pGPR.oRdData13 := mGPR(13.U(GPRS_WIDTH.W))
    io.pGPR.oRdData14 := mGPR(14.U(GPRS_WIDTH.W))
    io.pGPR.oRdData15 := mGPR(15.U(GPRS_WIDTH.W))
    io.pGPR.oRdData16 := mGPR(16.U(GPRS_WIDTH.W))
    io.pGPR.oRdData17 := mGPR(17.U(GPRS_WIDTH.W))
    io.pGPR.oRdData18 := mGPR(18.U(GPRS_WIDTH.W))
    io.pGPR.oRdData19 := mGPR(19.U(GPRS_WIDTH.W))
    io.pGPR.oRdData20 := mGPR(20.U(GPRS_WIDTH.W))
    io.pGPR.oRdData21 := mGPR(21.U(GPRS_WIDTH.W))
    io.pGPR.oRdData22 := mGPR(22.U(GPRS_WIDTH.W))
    io.pGPR.oRdData23 := mGPR(23.U(GPRS_WIDTH.W))
    io.pGPR.oRdData24 := mGPR(24.U(GPRS_WIDTH.W))
    io.pGPR.oRdData25 := mGPR(25.U(GPRS_WIDTH.W))
    io.pGPR.oRdData26 := mGPR(26.U(GPRS_WIDTH.W))
    io.pGPR.oRdData27 := mGPR(27.U(GPRS_WIDTH.W))
    io.pGPR.oRdData28 := mGPR(28.U(GPRS_WIDTH.W))
    io.pGPR.oRdData29 := mGPR(29.U(GPRS_WIDTH.W))
    io.pGPR.oRdData30 := mGPR(30.U(GPRS_WIDTH.W))
    io.pGPR.oRdData31 := mGPR(31.U(GPRS_WIDTH.W))
}
