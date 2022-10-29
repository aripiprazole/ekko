package ekko.parsing

import ekko.parsing.EkkoParser.AltContext
import ekko.parsing.tree.Alternative
import java.io.File

fun AltContext.treeToAlternative(file: File): Alternative {
  val name = name.treeToIdent(file)
  val pattern = pat().map { it.treeToPat(file) }
  val value = value.treeToExp(file)

  return Alternative(name, pattern, value, getLocationIn(file))
}
