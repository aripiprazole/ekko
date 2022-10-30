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

context(File)
fun ExpContext.treeToExpression(): Expression {
  return when (this) {
    is ELetContext -> {
      val names = alt().map { it.treeToAlternative() }.associateBy { it.id }
      val value = value.treeToExpression()

      Expression.Let(names, value, getLocationIn())
    }

    is EAppContext -> {
      val lhs = lhs.treeToExpression()
      val rhs = rhs.treeToExpression()

      Expression.Application(lhs, rhs, getLocationIn())
    }

    is EVarContext -> {
      val ident = value.treeToIdent()

      Expression.Variable(ident, getLocationIn())
    }

    is EGroupContext -> {
      val value = value.treeToExpression()

      Expression.Group(value, getLocationIn())
    }

    is EStringContext -> {
      // We use `.substring()` here, because the lexer includes the " characters at the start and at
      // the end of the string.
      val text = value.text.substring(1, value.text.length - 1)

      Expression.Literal(Literal.String(text, getLocationIn()))
    }

    is EDecimalContext -> {
      val float = value.text.toFloat()

      Expression.Literal(Literal.Float(float, getLocationIn()))
    }

    is EIntContext -> {
      val int = value.text.toInt()

      Expression.Literal(Literal.Int(int, getLocationIn()))
    }

    is EAbsContext -> {
      val param = param.treeToPattern()
      val value = value.treeToExpression()

      Expression.Abstraction(param, value, getLocationIn())
    }

    is EInfixContext -> {
      val callee = callee.treeToIdent()
      val lhs = lhs.treeToExpression()
      val rhs = rhs.treeToExpression()

      Expression.Application(
        lhs = Expression.Application(
          lhs = Expression.Variable(id = callee, location = callee.location),
          rhs = lhs,
          location = lhs.location.endIn(callee.location),
        ),
        rhs = rhs,
        location = getLocationIn(),
      )
    }

    else -> throw IllegalArgumentException("Unsupported expression: ${this::class}")
  }
}
