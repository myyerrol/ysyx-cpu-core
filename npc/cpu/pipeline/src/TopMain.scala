import circt.stage._

object TopMain extends App {
    println("Generating the CPU hardware")
    def top = new Top()
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
