package ekko

import ekko.parsing.EkkoLexer
import ekko.parsing.EkkoParser
import ekko.parsing.errors.SyntaxErrorListener
import ekko.parsing.tree.Exp
import ekko.parsing.treeToExp
import ekko.reporting.Report
import ekko.typing.Forall
import ekko.typing.Typ
import ekko.typing.Typer
import ekko.typing.arrow
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.DiagnosticErrorListener

fun readExp(input: String): Exp {
  val path = createTempFile("ekko", ".ekko").apply { writeText(input) }
  val file = path.toFile()

  val report = Report(file)

  val lexer = EkkoLexer(CharStreams.fromPath(path))
  val parser = EkkoParser(CommonTokenStream(lexer)).apply {
    addErrorListener(SyntaxErrorListener(report))
    addErrorListener(DiagnosticErrorListener())
  }

  val tree = parser.exp()

  if (report.isNotEmpty()) {
    report.show()

    error("Can't proceed due to syntax errors")
  }

  return tree.treeToExp(file)
}

fun main() {
  val exp = readExp("""let f x = x, a = f id in a (\x -> x)""")

  val env = buildMap {
    put("id", Forall("a") { Typ.variable("a") arrow Typ.variable("a") })
  }

  println(Typer().runInfer(exp, env))
}
