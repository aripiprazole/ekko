package ekko.parsing

import ekko.parsing.tree.Location
import ekko.parsing.tree.Position
import java.io.File
import org.antlr.v4.runtime.ParserRuleContext

fun ParserRuleContext.getLocationIn(file: File): Location {
  return Location(
    start = Position(start!!.startIndex, file),
    end = Position(stop!!.stopIndex, file),
  )
}
