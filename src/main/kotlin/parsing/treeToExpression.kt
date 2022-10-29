package ekko.parsing

import ekko.parsing.EkkoParser.EAbsContext
import ekko.parsing.EkkoParser.EAppContext
import ekko.parsing.EkkoParser.EDecimalContext
import ekko.parsing.EkkoParser.EGroupContext
import ekko.parsing.EkkoParser.EInfixContext
import ekko.parsing.EkkoParser.EIntContext
import ekko.parsing.EkkoParser.ELetContext
import ekko.parsing.EkkoParser.EStringContext
import ekko.parsing.EkkoParser.EVarContext
import ekko.parsing.EkkoParser.ExpContext
import ekko.parsing.tree.Expression
import ekko.parsing.tree.Literal
import java.io.File

fun ExpContext.treeToExp(file: File): Expression {
  return when (this) {
    is ELetContext -> {
      val names = alt().map { it.treeToAlternative(file) }.associateBy { it.id }
      val value = value.treeToExp(file)

      Expression.Let(names, value, getLocationIn(file))
    }

    is EAppContext -> {
      val lhs = lhs.treeToExp(file)
      val rhs = rhs.treeToExp(file)

      Expression.Application(lhs, rhs, getLocationIn(file))
    }

    is EVarContext -> {
      val ident = value.treeToIdent(file)

      Expression.Variable(ident, getLocationIn(file))
    }

    is EGroupContext -> {
      val value = value.treeToExp(file)

      Expression.Group(value, getLocationIn(file))
    }

    is EStringContext -> {
      // We use `.substring()` here, because the lexer includes the " characters at the start and at
      // the end of the string.
      val text = value.text.substring(1, value.text.length - 1)

      Expression.Literal(Literal.String(text, getLocationIn(file)))
    }

    is EDecimalContext -> {
      val float = value.text.toFloat()

      Expression.Literal(Literal.Float(float, getLocationIn(file)))
    }

    is EIntContext -> {
      val int = value.text.toInt()

      Expression.Literal(Literal.Int(int, getLocationIn(file)))
    }

    is EAbsContext -> {
      val param = param.treeToPat(file)
      val value = value.treeToExp(file)

      Expression.Abstraction(param, value, getLocationIn(file))
    }

    is EInfixContext -> {
      val callee = callee.treeToIdent(file)
      val lhs = lhs.treeToExp(file)
      val rhs = rhs.treeToExp(file)

      Expression.Application(
        lhs = Expression.Application(
          lhs = Expression.Variable(id = callee, location = callee.location),
          rhs = lhs,
          location = lhs.location.endIn(callee.location),
        ),
        rhs = rhs,
        location = getLocationIn(file),
      )
    }

    else -> throw IllegalArgumentException("Unsupported expression: ${this::class}")
  }
}
