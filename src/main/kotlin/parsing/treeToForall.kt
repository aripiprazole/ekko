package ekko.parsing

import ekko.parsing.EkkoParser.ForallContext
import ekko.parsing.EkkoParser.SQuantifierContext
import ekko.parsing.EkkoParser.STypeContext
import ekko.parsing.tree.ParsedForall
import java.io.File

context(File)
fun ForallContext.treeToForall(): ParsedForall {
  when (this) {
    is SQuantifierContext -> {
      val names = ident().map { it.treeToIdent() }.toSet()
      val type = type.treeToType()

      return ParsedForall(names, type, getLocationIn())
    }

    is STypeContext -> {
      val type = value.treeToType()

      return ParsedForall(emptySet(), type, getLocationIn())
    }

    else -> throw IllegalArgumentException("Unsupported scheme: ${this::class}")
  }
}
