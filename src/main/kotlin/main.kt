package ekko

import ekko.parsing.EkkoLexer
import ekko.parsing.EkkoParser
import ekko.parsing.errors.SyntaxErrorListener
import ekko.parsing.tree.Expression
import ekko.parsing.treeToExp
import ekko.reporting.Report
import ekko.typing.Forall
import ekko.typing.Type
import ekko.typing.Typer
import ekko.typing.arrow
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.DiagnosticErrorListener

fun main() {
  val exp = readExp("let x: a -> a = 10 in x")

  val env = buildMap {
    put("sum", Forall { Type.Int arrow (Type.Int arrow Type.Int) })
  }

  println(Typer().runInfer(exp, env))
}

fun readExp(input: String): Expression {
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
