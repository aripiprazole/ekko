package ekko

import ekko.parsing.EkkoLexer
import ekko.parsing.EkkoParser
import ekko.parsing.tree.Exp
import ekko.parsing.treeToExp
import ekko.typing.Forall
import ekko.typing.Infer
import ekko.typing.Typ
import ekko.typing.arrow
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.DiagnosticErrorListener

fun readExp(input: String): Exp {
  val path = createTempFile("ekko", ".ekko").apply { writeText(input) }

  val lexer = EkkoLexer(CharStreams.fromPath(path))
  val parser = EkkoParser(CommonTokenStream(lexer)).apply {
    addErrorListener(DiagnosticErrorListener())
  }

  return parser.exp().treeToExp(path.toFile())
}

fun main() {
  val exp = readExp("""let f x = x, a = f id in a (\x -> x)""")
  val infer = Infer()

  val env = buildMap {
    put("id", Forall(setOf("a"), Typ.variable("a") arrow Typ.variable("a")))
  }

  println(infer.tiExp(exp, env))
}
