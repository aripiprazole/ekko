package ekko.parsing

import ekko.parsing.tree.Location
import ekko.parsing.tree.Position
import java.io.File
import org.antlr.v4.runtime.ParserRuleContext

context(File)
fun ParserRuleContext.getLocationIn(): Location {
  return Location(
    start = Position(start.startIndex, this@File),
    end = Position(stop.stopIndex, this@File),
  )
}
