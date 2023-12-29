import chisel3._
import circt.stage._

import cpu.common._

object TopMain extends App with Build {
    println("Generating the CPU hardware")

    if (CPU_TYPE.equals("single")) {
        def top = new cpu.stage.single.Top()
        val generator = Seq(chisel3.stage.ChiselGeneratorAnnotation(() => top))
        val useMFC = true
        if (useMFC) {
            (new ChiselStage).execute(
                args,
                generator :+ CIRCTTargetAnnotation(CIRCTTarget.Verilog))
        }
        else {
            (new chisel3.stage.ChiselStage).execute(args, generator)
        }
    }
    else if (CPU_TYPE.equals("multip")) {
        def top = new cpu.stage.multip.Top()
        val generator = Seq(chisel3.stage.ChiselGeneratorAnnotation(() => top))
        val useMFC = true
        if (useMFC) {
            (new ChiselStage).execute(
                args,
                generator :+ CIRCTTargetAnnotation(CIRCTTarget.Verilog))
        }
        else {
            (new chisel3.stage.ChiselStage).execute(args, generator)
        }
    }
    else if (CPU_TYPE.equals("pipeline")) {
        def top = new cpu.stage.pipeline.Top()
        val generator = Seq(chisel3.stage.ChiselGeneratorAnnotation(() => top))
        val useMFC = true
        if (useMFC) {
            (new ChiselStage).execute(
                args,
                generator :+ CIRCTTargetAnnotation(CIRCTTarget.Verilog))
        }
        else {
            (new chisel3.stage.ChiselStage).execute(args, generator)
        }
    }
}
