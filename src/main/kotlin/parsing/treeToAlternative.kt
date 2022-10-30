package ekko.parsing

import ekko.parsing.EkkoParser.AInferContext
import ekko.parsing.EkkoParser.ATypedContext
import ekko.parsing.EkkoParser.AltContext
import ekko.parsing.tree.Alternative
import java.io.File

context(File)
fun AltContext.treeToAlternative(): Alternative {
  when (this) {
    is AInferContext -> {
      val name = name.treeToIdent()
      val pattern = pat().map { it.treeToPattern() }
      val value = value.treeToExpression()

      return Alternative(name, pattern, value, getLocationIn())
    }

    is ATypedContext -> {
      val name = name.treeToIdent()
      val type = type.treeToForall()
      val value = value.treeToExpression()

      return Alternative(name, emptyList(), value, getLocationIn(), type)
    }

    else -> throw IllegalArgumentException("Unsupported alternative: ${this::class}")
  }
}
