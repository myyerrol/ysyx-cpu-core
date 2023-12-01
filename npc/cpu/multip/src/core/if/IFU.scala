package cpu.core

import chisel3._
import chisel3.util._

import cpu.blackbox._
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
        val iMemRdEn       =  Input(UInt(SIGS_WIDTH.W))
        val iMemRdSrc      =  Input(UInt(SIGS_WIDTH.W))
        val iIRWrEn        =  Input(Bool())

        val iPCNext        =  Input(UInt(DATA_WIDTH.W))
        val iPCJump        =  Input(UInt(DATA_WIDTH.W))
        val iALUZero       =  Input(Bool())
        val iInst          =  Input(UInt(DATA_WIDTH.W))

        val bIFUIO            = new IFUIO
        val bIFUAXIMasterARIO = new IFUAXI4LiteARIO
        val bIFUAXIMasterRIO  = new IFUAXI4LiteRIO

        val oState         = Output(UInt(2.W))
    })


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
    }
    .otherwise {
        io.bIFUIO.oPC := rPC
    }

    val mIRU = Module(new IRU)
    mIRU.io.iWrEn := io.iIRWrEn
    mIRU.io.iData := DontCare

    // val mIRU = Module(new IRU)
    // mIRU.io.iWrEn := io.iIRWrEn
    // // mIRU.io.iData := io.iInst

    io.bIFUAXIMasterARIO.arvalid := DontCare
    io.bIFUAXIMasterARIO.araddr  := DontCare
    io.bIFUAXIMasterRIO.rready   := true.B

    when ((io.iMemRdEn === true.B) && (io.iMemRdSrc === MEM_RD_SRC_PC)) {
        io.bIFUAXIMasterARIO.arvalid := true.B
        io.bIFUAXIMasterARIO.araddr  := rPC
    }

    val stateARWait :: stateAWait :: stateA :: Nil = Enum(3)
    val state = RegInit(stateARWait)

    io.oState := state

    switch (state) {
        is (stateARWait) {
            when (io.bIFUAXIMasterARIO.arvalid &&
                  io.bIFUAXIMasterARIO.arready) {
                state := stateAWait
            }
            .otherwise {
                state := stateARWait
            }
        }
        is (stateAWait) {
            when (io.bIFUAXIMasterRIO.rvalid &&
                  io.bIFUAXIMasterRIO.rready) {
                state := stateA
                // when (io.bIFUAXIMasterRIO.rresp === 0.U) {
                //     mIRU.io.iData := io.bIFUAXIMasterRIO.rdata
                // }
                // io.bIFUAXIMasterARIO.arvalid := false.B
            }
            .otherwise {
                state := stateAWait
            }
        }
        is (stateA) {
            state := stateARWait
            when (io.bIFUAXIMasterRIO.rresp === 0.U) {
                mIRU.io.iData := io.bIFUAXIMasterRIO.rdata
            }
        }
    }

    printf("state: %d\n", state)
    printf("mIRU.io.iData: %x\n", mIRU.io.iData)

    io.bIFUIO.oInst := mIRU.io.oData
}
