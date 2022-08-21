package ekko.parser

import ekko.parser.EkkoParser.PVarContext
import ekko.parser.EkkoParser.PatContext
import ekko.tree.PVar
import ekko.tree.Pat

fun PatContext.treeToPat(): Pat {
  return when (this) {
    is PVarContext -> {
      val name = name!!.treeToIdent()

      PVar(name)
    }

    else -> throw IllegalArgumentException("Unsupported pattern: ${this::class}")
  }
}
