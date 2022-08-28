package ekko.parser

import ekko.parser.EkkoParser.AltContext
import ekko.tree.Alt
import java.io.File

fun AltContext.treeToAlt(file: File): Alt {
  val name = name!!.treeToIdent(file)
  val pattern = findPat().map { it.treeToPat(file) }
  val value = value!!.treeToExp(file)

  return Alt(name, pattern, value, getLocationIn(file))
}
