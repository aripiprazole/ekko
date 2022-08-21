package ekko

import ekko.parser.EkkoLexer
import ekko.parser.EkkoParser
import ekko.parser.toParseTree
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.DiagnosticErrorListener

fun main() {
  val stream = CharStreams.fromString(
    """
    println "hello, world!"
    """.trimIndent(),
  )

  val lexer = EkkoLexer(stream)
  val parser = EkkoParser(CommonTokenStream(lexer)).apply {
    addErrorListener(DiagnosticErrorListener())
  }

  val exp = parser.exp()

  println(exp.toParseTree().multilineString())
}
