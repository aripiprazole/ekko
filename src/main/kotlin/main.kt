package ekko

import ekko.parsing.EkkoLexer
import ekko.parsing.EkkoParser
import ekko.parsing.errors.SyntaxErrorListener
import ekko.parsing.treeToExpression
import ekko.reporting.Report
import ekko.typing.Typer
import ekko.typing.tree.Forall
import ekko.typing.tree.Type
import ekko.typing.tree.arrow
import java.io.File
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.DiagnosticErrorListener

fun main() {
  val exp = readExp("let x: ∀ a. a -> a = 10 in x", EkkoParser::exp) { treeToExpression() }

  val env = buildMap {
    put("sum", Forall { Type.Int arrow (Type.Int arrow Type.Int) })
  }

  println(Typer().runInfer(exp, env))
}

fun <E, O> readExp(input: String, f: EkkoParser.() -> E, g: context(File) E.() -> O): O {
  val path = createTempFile("ekko", ".ekko").apply { writeText(input) }
  val file = path.toFile()

  val report = Report(file)

  val lexer = EkkoLexer(CharStreams.fromPath(path))
  val parser = EkkoParser(CommonTokenStream(lexer)).apply {
    addErrorListener(SyntaxErrorListener(report))
    addErrorListener(DiagnosticErrorListener())
  }

  val tree = f(parser)

  if (report.isNotEmpty()) {
    report.show()

    error("Can't proceed due to syntax errors")
  }

  return g(file, tree)
}
