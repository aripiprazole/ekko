package ekko.parsing

import ekko.parsing.EkkoParser.PVarContext
import ekko.parsing.EkkoParser.PatContext
import ekko.parsing.tree.Pattern
import java.io.File

context(File)
fun PatContext.treeToPattern(): Pattern {
  return when (this) {
    is PVarContext -> {
      val name = name.treeToIdent()

      Pattern.Variable(name, getLocationIn())
    }

    else -> throw IllegalArgumentException("Unsupported pattern: ${this::class}")
  }
}
