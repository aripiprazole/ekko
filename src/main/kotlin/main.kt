package ekko

import ekko.parsing.EkkoLexer
import ekko.parsing.EkkoParser
import ekko.parsing.errors.SyntaxError
import ekko.parsing.tree.Exp
import ekko.parsing.treeToExp
import ekko.reporting.Report
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

fun readExp(input: String): Pair<File, Exp> {
  val path = createTempFile("ekko", ".ekko").apply { writeText(input) }
  val file = path.toFile()

  val lexer = EkkoLexer(CharStreams.fromPath(path))
  val parser = EkkoParser(CommonTokenStream(lexer)).apply {
    addErrorListener(DiagnosticErrorListener())
  }

  return file to parser.exp().treeToExp(file)
}

fun main() {
  val (file, exp) = readExp("""let f x = x, a = f id in a (\x -> x)""")

  Report
    .build(file) {
      addMessage(1..5) { SyntaxError("Expecting element") }
    }
    .show()

  val env = buildMap {
    put("id", Forall("a") { Typ.variable("a") arrow Typ.variable("a") })
  }

  println(Infer().tiExp(exp, env).second)
}
