package ekko.parsing

import ekko.parsing.EkkoParser.TAppContext
import ekko.parsing.EkkoParser.TGroupContext
import ekko.parsing.EkkoParser.TInfixContext
import ekko.parsing.EkkoParser.TVarContext
import ekko.parsing.EkkoParser.TypContext
import ekko.parsing.tree.ParsedType
import java.io.File

context(File)
fun TypContext.treeToType(): ParsedType {
  when (this) {
    is TVarContext -> {
      val name = name.treeToIdent()

      return ParsedType.Variable(name)
    }

    is TAppContext -> {
      val lhs = lhs.treeToType()
      val rhs = rhs.treeToType()

      return ParsedType.Application(lhs, rhs, getLocationIn())
    }

    is TInfixContext -> {
      val lhs = lhs.treeToType()
      val callee = callee.treeToIdent()
      val rhs = rhs.treeToType()

      return ParsedType.Application(
        lhs = ParsedType.Application(
          lhs = ParsedType.Variable(callee),
          rhs = lhs,
          location = lhs.location.endIn(callee.location),
        ),
        rhs = rhs,
        location = getLocationIn(),
      )
    }

    is TGroupContext -> {
      val value = value.treeToType()

      return ParsedType.Group(value, getLocationIn())
    }

    else -> throw IllegalArgumentException("Unsupported type: ${this::class}")
  }
}
