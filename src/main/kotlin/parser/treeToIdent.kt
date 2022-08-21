package ekko.parser

import ekko.tree.Ident
import org.antlr.v4.kotlinruntime.Token

fun Token.treeToIdent(): Ident {
  return Ident(text!!)
}
