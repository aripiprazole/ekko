package ekko.parsing

import ekko.tree.Ident
import ekko.tree.Location
import ekko.tree.Position
import java.io.File
import org.antlr.v4.kotlinruntime.Token

fun Token.treeToIdent(file: File): Ident {
  return Ident(
    text!!,
    location = Location(
      start = Position(startIndex, file),
      end = Position(stopIndex, file),
    ),
  )
}
