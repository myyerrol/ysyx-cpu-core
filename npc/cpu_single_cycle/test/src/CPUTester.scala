import chisel3._
import chiseltest._

import utest._

import utils.Base._

object CPUTester extends ChiselUtestTester {
    val tests = Tests {
        test("IFU") {
            testCircuit(new IFU()) { dut =>
                for (i <- 0 to 20) {
                    dut.io.iPC.poke(i.U)
                    dut.io.oPC.expect(i.U)
                }
            }
        }
        test("IDU") {
            testCircuit(new IDU()) { dut =>
                // addi
                // rd[1] = rs1[0] + 1
                // 000000000001 00000 000 00001 0010011
                // 0000 0000 0001 0000 0000 0000 1001 0011
                dut.io.iInst.poke(0x00100093.U)
                dut.io.oInstType.expect(ADDI.U)
                dut.io.oInstRS1.expect(0.U)
                dut.io.oInstRS2.expect(0.U)
                dut.io.oInstRD.expect(1.U)
                dut.io.oInstImm.expect(1.U)
                dut.clock.step(1)
                // addi
                // rd[3] = rs1[0] + 10
                // 000000001010 00000 000 00011 001 0011
                // 0000 0000 1010 0000 0000 0001 1001 0011
                dut.io.iInst.poke(0x00A00193.U)
                dut.io.oInstType.expect(ADDI.U)
                dut.io.oInstRS1.expect(0.U)
                dut.io.oInstRS2.expect(0.U)
                dut.io.oInstRD.expect(3.U)
                dut.io.oInstImm.expect(10.U)
            }
        }
        test("EXU") {
            testCircuit(new EXU()) { dut =>
                // rd[1] = rs1[0] + 1
                dut.io.iInstType.poke(ADDI.U)
                dut.io.iInstRS1.poke(0.U)
                dut.io.iInstRS2.poke(0.U)
                dut.io.iInstRD.poke(1.U)
                dut.io.iInstImm.poke(1.U)
                dut.io.oInstRDVal.expect(1.U)
                dut.io.oHalt.expect(false.B)
                dut.clock.step(1)
                // rd[3] = rs1[0] + 10
                dut.io.iInstType.poke(ADDI.U)
                dut.io.iInstRS1.poke(0.U)
                dut.io.iInstRS2.poke(0.U)
                dut.io.iInstRD.poke(3.U)
                dut.io.iInstImm.poke(10.U)
                dut.io.oInstRDVal.expect(10.U)
                dut.io.oHalt.expect(false.B)
            }
        }
        test("Top") {
            testCircuit(new Top()) { dut =>
                // // rd[1] = rs1[0] + 1
                dut.io.iInst.poke(0x00100093.U)
                dut.clock.step(1)
                // rd[3] = rs1[0] + 10
                dut.io.iInst.poke(0x00A00193.U)
                dut.clock.step(1)
                // ebreak
                dut.io.iInst.poke(0x00100073.U)
                dut.clock.step(1)
            }
        }
    }
}
