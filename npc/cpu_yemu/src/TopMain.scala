import circt.stage._

object TopMain extends App {
    def top = new Top()
    val generator = Seq(chisel3.stage.ChiselGeneratorAnnotation(() => top))
    (new ChiselStage).execute(
        args,
        generator :+ CIRCTTargetAnnotation(CIRCTTarget.Verilog))
}
