package ekko.parsing

import ekko.parsing.EkkoParser.PVarContext
import ekko.parsing.EkkoParser.PatContext
import ekko.parsing.tree.Pattern
import java.io.File

fun PatContext.treeToPattern(file: File): Pattern {
  return when (this) {
    is PVarContext -> {
      val name = name.treeToIdent(file)

      Pattern.Variable(name, getLocationIn(file))
    }

    else -> throw IllegalArgumentException("Unsupported pattern: ${this::class}")
  }
}
