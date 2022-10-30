package ekko.parsing

import ekko.parsing.EkkoParser.AInferContext
import ekko.parsing.EkkoParser.ATypedContext
import ekko.parsing.EkkoParser.AltContext
import ekko.parsing.tree.Alternative
import java.io.File

fun AltContext.treeToAlternative(file: File): Alternative {
  when (this) {
    is AInferContext -> {
      val name = name.treeToIdent(file)
      val pattern = pat().map { it.treeToPattern(file) }
      val value = value.treeToExpression(file)

      return Alternative(name, pattern, value, getLocationIn(file))
    }

    is ATypedContext -> {
      val name = name.treeToIdent(file)
      val type = type.treeToForall(file)
      val value = value.treeToExpression(file)

      return Alternative(name, emptyList(), value, getLocationIn(file), type)
    }

    else -> throw IllegalArgumentException("Unsupported alternative: ${this::class}")
  }
}
