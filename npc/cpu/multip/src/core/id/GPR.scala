package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class GPRIO extends Bundle with ConfigIO {
    val oRdData0   = Output(UInt(DATA_WIDTH.W))
    val oRdData1   = Output(UInt(DATA_WIDTH.W))
    val oRdData2   = Output(UInt(DATA_WIDTH.W))
    val oRdData3   = Output(UInt(DATA_WIDTH.W))
    val oRdData4   = Output(UInt(DATA_WIDTH.W))
    val oRdData5   = Output(UInt(DATA_WIDTH.W))
    val oRdData6   = Output(UInt(DATA_WIDTH.W))
    val oRdData7   = Output(UInt(DATA_WIDTH.W))
    val oRdData8   = Output(UInt(DATA_WIDTH.W))
    val oRdData9   = Output(UInt(DATA_WIDTH.W))
    val oRdData10  = Output(UInt(DATA_WIDTH.W))
    val oRdData11  = Output(UInt(DATA_WIDTH.W))
    val oRdData12  = Output(UInt(DATA_WIDTH.W))
    val oRdData13  = Output(UInt(DATA_WIDTH.W))
    val oRdData14  = Output(UInt(DATA_WIDTH.W))
    val oRdData15  = Output(UInt(DATA_WIDTH.W))
    val oRdData16  = Output(UInt(DATA_WIDTH.W))
    val oRdData17  = Output(UInt(DATA_WIDTH.W))
    val oRdData18  = Output(UInt(DATA_WIDTH.W))
    val oRdData19  = Output(UInt(DATA_WIDTH.W))
    val oRdData20  = Output(UInt(DATA_WIDTH.W))
    val oRdData21  = Output(UInt(DATA_WIDTH.W))
    val oRdData22  = Output(UInt(DATA_WIDTH.W))
    val oRdData23  = Output(UInt(DATA_WIDTH.W))
    val oRdData24  = Output(UInt(DATA_WIDTH.W))
    val oRdData25  = Output(UInt(DATA_WIDTH.W))
    val oRdData26  = Output(UInt(DATA_WIDTH.W))
    val oRdData27  = Output(UInt(DATA_WIDTH.W))
    val oRdData28  = Output(UInt(DATA_WIDTH.W))
    val oRdData29  = Output(UInt(DATA_WIDTH.W))
    val oRdData30  = Output(UInt(DATA_WIDTH.W))
    val oRdData31  = Output(UInt(DATA_WIDTH.W))
}

class GPR extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iWrEn      =  Input(Bool())
        val iRd1Addr   =  Input(UInt(DATA_WIDTH.W))
        val iRd2Addr   =  Input(UInt(DATA_WIDTH.W))
        val iWrAddr    =  Input(UInt(DATA_WIDTH.W))
        val iWrData    =  Input(UInt(DATA_WIDTH.W))

        val oRd1Data   = Output(UInt(DATA_WIDTH.W))
        val oRd2Data   = Output(UInt(DATA_WIDTH.W))
        val oRdEndData = Output(UInt(DATA_WIDTH.W))

        val bGPRIO      = new GPRIO
    })

    val mGPR = Mem(GPRS_NUM, UInt(DATA_WIDTH.W))
    mGPR(0.U) := 0.U(DATA_WIDTH.W)

    when (io.iWrEn) {
        mGPR(io.iWrAddr) := io.iWrData
    }

    io.oRd1Data   := mGPR(io.iRd1Addr)
    io.oRd2Data   := mGPR(io.iRd2Addr)
    io.oRdEndData := mGPR(GPRS_10)

    io.bGPRIO.oRdData0  := mGPR( 0.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData1  := mGPR( 1.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData2  := mGPR( 2.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData3  := mGPR( 3.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData4  := mGPR( 4.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData5  := mGPR( 5.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData6  := mGPR( 6.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData7  := mGPR( 7.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData8  := mGPR( 8.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData9  := mGPR( 9.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData10 := mGPR(10.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData11 := mGPR(11.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData12 := mGPR(12.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData13 := mGPR(13.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData14 := mGPR(14.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData15 := mGPR(15.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData16 := mGPR(16.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData17 := mGPR(17.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData18 := mGPR(18.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData19 := mGPR(19.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData20 := mGPR(20.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData21 := mGPR(21.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData22 := mGPR(22.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData23 := mGPR(23.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData24 := mGPR(24.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData25 := mGPR(25.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData26 := mGPR(26.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData27 := mGPR(27.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData28 := mGPR(28.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData29 := mGPR(29.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData30 := mGPR(30.U(GPRS_WIDTH.W))
    io.bGPRIO.oRdData31 := mGPR(31.U(GPRS_WIDTH.W))
}
