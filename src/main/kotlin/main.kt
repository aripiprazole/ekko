package ekko

import ekko.parsing.EkkoLexer
import ekko.parsing.EkkoParser
import ekko.parsing.tree.Exp
import ekko.parsing.tree.Location
import ekko.parsing.tree.Position
import ekko.parsing.treeToExp
import ekko.reporting.highlight
import ekko.typing.Forall
import ekko.typing.Infer
import ekko.typing.Typ
import ekko.typing.arrow
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.DiagnosticErrorListener

fun readExp(input: String, runWithFile: File.() -> Unit = {}): Exp {
  val path = createTempFile("ekko", ".ekko").apply { writeText(input) }

  runWithFile(path.toFile())

  val lexer = EkkoLexer(CharStreams.fromPath(path))
  val parser = EkkoParser(CommonTokenStream(lexer)).apply {
    addErrorListener(DiagnosticErrorListener())
  }

  return parser.exp().treeToExp(path.toFile())
}

fun main() {
  val exp = readExp("""let f x = x, a = f id in a (\x -> x)""") {
    highlight(Location(Position(1, 1), Position(1, 4))) {
      "Can not unify String with Bool"
    }
  }

  val infer = Infer()

  val env = buildMap {
    put("id", Forall("a") { Typ.variable("a") arrow Typ.variable("a") })
  }

  println(infer.tiExp(exp, env).second)
}
