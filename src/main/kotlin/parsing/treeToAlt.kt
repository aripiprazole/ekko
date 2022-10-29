package ekko.parsing

import ekko.parsing.EkkoParser.AltContext
import ekko.parsing.tree.Alt
import java.io.File

fun AltContext.treeToAlt(file: File): Alt {
  val name = name!!.treeToIdent(file)
  val pattern = pat().map { it.treeToPat(file) }
  val value = value!!.treeToExp(file)

  return Alt(name, pattern, value, getLocationIn(file))
}
