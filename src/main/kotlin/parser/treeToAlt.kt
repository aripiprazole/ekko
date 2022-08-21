package ekko.parser

import ekko.parser.EkkoParser.AltContext
import ekko.tree.Alt

fun AltContext.treeToAlt(): Alt {
  val name = name!!.treeToIdent()
  val pattern = findPat().map { it.treeToPat() }
  val value = value!!.treeToExp()

  return Alt(name, pattern, value)
}
