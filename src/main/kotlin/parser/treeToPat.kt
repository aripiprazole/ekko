package ekko.parser

import ekko.parser.EkkoParser.PVarContext
import ekko.parser.EkkoParser.PatContext
import ekko.tree.PVar
import ekko.tree.Pat
import java.io.File

fun PatContext.treeToPat(file: File): Pat {
  return when (this) {
    is PVarContext -> {
      val name = name!!.treeToIdent(file)

      PVar(name, getLocationIn(file))
    }

    else -> throw IllegalArgumentException("Unsupported pattern: ${this::class}")
  }
}
