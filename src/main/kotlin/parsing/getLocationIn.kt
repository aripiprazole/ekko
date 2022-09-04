package ekko.parsing

import ekko.tree.Location
import ekko.tree.Position
import java.io.File
import org.antlr.v4.kotlinruntime.ParserRuleContext

fun ParserRuleContext.getLocationIn(file: File): Location {
  return Location(
    start = Position(start!!.startIndex, file),
    end = Position(stop!!.stopIndex, file),
  )
}
