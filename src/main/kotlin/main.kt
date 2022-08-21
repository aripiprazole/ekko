package ekko

import ekko.parser.EkkoLexer
import ekko.parser.EkkoParser
import ekko.parser.treeToExp
import ekko.typing.Forall
import ekko.typing.Infer
import ekko.typing.Typ
import ekko.typing.arrow
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.DiagnosticErrorListener

fun main() {
  val stream = CharStreams.fromString("""let f x = x, a = f id in a 1""")

  val lexer = EkkoLexer(stream)
  val parser = EkkoParser(CommonTokenStream(lexer)).apply {
    addErrorListener(DiagnosticErrorListener())
  }
  val infer = Infer()

  val exp = parser.exp()

  val env = buildMap {
    put("id", Forall(setOf("a"), Typ.variable("a") arrow Typ.variable("a")))
  }

  println(infer.synthExp(exp.treeToExp(), env))
}
