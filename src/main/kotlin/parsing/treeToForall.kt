package ekko.parsing

import ekko.parsing.EkkoParser.ForallContext
import ekko.parsing.EkkoParser.SQuantifierContext
import ekko.parsing.EkkoParser.STypeContext
import ekko.parsing.tree.ParsedForall
import java.io.File

fun ForallContext.treeToForall(file: File): ParsedForall {
  when (this) {
    is SQuantifierContext -> {
      val names = ident().map { it.treeToIdent(file) }.toSet()
      val type = type.treeToType(file)

      return ParsedForall(names, type, getLocationIn(file))
    }

    is STypeContext -> {
      val type = value.treeToType(file)

      return ParsedForall(emptySet(), type, getLocationIn(file))
    }

    else -> throw IllegalArgumentException("Unsupported scheme: ${this::class}")
  }
}
