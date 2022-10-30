package ekko.parsing

import ekko.parsing.EkkoParser.TAppContext
import ekko.parsing.EkkoParser.TGroupContext
import ekko.parsing.EkkoParser.TInfixContext
import ekko.parsing.EkkoParser.TVarContext
import ekko.parsing.EkkoParser.TypContext
import ekko.parsing.tree.ParsedType
import java.io.File

fun TypContext.treeToType(file: File): ParsedType {
  when (this) {
    is TVarContext -> {
      val name = name.treeToIdent(file)

      return ParsedType.Variable(name)
    }

    is TAppContext -> {
      val lhs = lhs.treeToType(file)
      val rhs = rhs.treeToType(file)

      return ParsedType.Application(lhs, rhs, getLocationIn(file))
    }

    is TInfixContext -> {
      val lhs = lhs.treeToType(file)
      val callee = callee.treeToIdent(file)
      val rhs = rhs.treeToType(file)

      return ParsedType.Application(
        lhs = ParsedType.Application(
          lhs = ParsedType.Variable(callee),
          rhs = lhs,
          location = lhs.location.endIn(callee.location),
        ),
        rhs = rhs,
        location = getLocationIn(file),
      )
    }

    is TGroupContext -> {
      val value = value.treeToType(file)

      return ParsedType.Group(value, getLocationIn(file))
    }

    else -> throw IllegalArgumentException("Unsupported type: ${this::class}")
  }
}
