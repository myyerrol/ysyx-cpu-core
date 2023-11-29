package cpu.core

import chisel3._
import chisel3.util._

import cpu.common._

class IFUIO extends Bundle with ConfigIO {
    val oPC   = Output(UInt(DATA_WIDTH.W))
    val oInst = Output(UInt(DATA_WIDTH.W))
}

class IFUAXI4LiteARIO extends Bundle with ConfigIO {
    val arready =  Input(Bool())

    val arvalid = Output(Bool())
    val araddr  = Output(UInt(DATA_WIDTH.W))
}

class IFUAXI4LiteRIO extends Bundle with ConfigIO {
    val rvalid = Input(Bool())
    val rdata  = Input(UInt(INST_WIDTH.W))
    val rresp  = Input(UInt(2.W))

    val rready = Output(Bool())
}

class IFU extends Module with ConfigInst {
    val io = IO(new Bundle {
        val iInstName      =  Input(UInt(SIGS_WIDTH.W))
        val iPCWrEn        =  Input(Bool())
        val iPCWrSrc       =  Input(UInt(SIGS_WIDTH.W))
        val iIRWrEn        =  Input(Bool())

        val iPCNext        =  Input(UInt(DATA_WIDTH.W))
        val iPCJump        =  Input(UInt(DATA_WIDTH.W))
        val iALUZero       =  Input(Bool())
        val iInst          =  Input(UInt(DATA_WIDTH.W))

        val bIFUIO            = new IFUIO
        // val bIFUAXIMasterARIO = new IFUAXI4LiteARIO
        // val bIFUAXIMasterAIO  = new IFUAXI4LiteRIO
    })

    // val stateMasterIDLE :: stateMasterWait :: Nil = Enum(2)
    // val stateMaster = RegInit(stateMasterIDLE)
    // stateMaster := MuxLookup(
    //     stateMaster,
    //     stateMasterIDLE,
    //     Seq(
    //         stateMasterIDLE -> Mux(io.bIFUAXIMasterARIO.arvalid, stateMasterWait, stateMasterIDLE),
    //         stateMasterWait -> Mux(io.bIFUAXIMasterARIO.arready, stateMasterIDLE, stateMasterWait)
    //     )
    // )

    val rPC  = RegInit(ADDR_SIM)
    val wNPC = WireInit(ADDR_SIM)

    when (io.iPCWrSrc === PC_WR_SRC_NEXT) {
        wNPC := io.iPCNext
    }
    .elsewhen (io.iPCWrSrc === PC_WR_SRC_JUMP) {
        when (io.iInstName === INST_NAME_BEQ  ||
              io.iInstName === INST_NAME_BNE  ||
              io.iInstName === INST_NAME_BLT  ||
              io.iInstName === INST_NAME_BGE  ||
              io.iInstName === INST_NAME_BLTU ||
              io.iInstName === INST_NAME_BGEU) {
            wNPC := Mux(io.iALUZero === 1.U, io.iPCJump, io.iPCNext)
        }
        .otherwise {
            wNPC := io.iPCJump
        }
    }

    when (io.iPCWrEn) {
        rPC           := wNPC
        io.bIFUIO.oPC := rPC

        // io.bIFUAXIMasterARIO.arvalid := true.B
        // io.bIFUAXIMasterARIO.araddr  := rPC
    }
    .otherwise {
        io.bIFUIO.oPC := rPC

        // io.bIFUAXIMasterARIO.arvalid := false.B
        // io.bIFUAXIMasterARIO.araddr  := rPC
    }

    val mIRU = Module(new IRU)
    mIRU.io.iWrEn := io.iIRWrEn
    mIRU.io.iData := io.iInst

    io.bIFUIO.oInst := mIRU.io.oData
}
