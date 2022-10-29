package ekko.parsing

import ekko.parsing.EkkoParser.IdentContext
import ekko.parsing.EkkoParser.InfixIdentContext
import ekko.parsing.tree.Ident
import ekko.parsing.tree.Location
import ekko.parsing.tree.Position
import java.io.File
import org.antlr.v4.runtime.Token

fun IdentContext.treeToIdent(file: File): Ident {
  val text = if (text.startsWith("(")) text.substring(1, text.length - 1) else text

  return Ident(text, location = getLocationIn(file))
}

fun InfixIdentContext.treeToIdent(file: File): Ident {
  return Ident(text, location = getLocationIn(file))
}

fun Token.treeToIdent(file: File): Ident {
  return Ident(
    text,
    location = Location(
      start = Position(startIndex, file),
      end = Position(stopIndex, file),
    ),
  )
}
