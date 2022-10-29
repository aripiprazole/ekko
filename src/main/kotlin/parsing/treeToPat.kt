package ekko.parsing

import ekko.parsing.EkkoParser.PVarContext
import ekko.parsing.EkkoParser.PatContext
import ekko.parsing.tree.Pat
import java.io.File

fun PatContext.treeToPat(file: File): Pat {
  return when (this) {
    is PVarContext -> {
      val name = name.treeToIdent(file)

      Pat.Variable(name, getLocationIn(file))
    }

    else -> throw IllegalArgumentException("Unsupported pattern: ${this::class}")
  }
}
