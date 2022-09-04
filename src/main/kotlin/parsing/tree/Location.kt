package ekko.parsing.tree

import java.io.File

data class Location(val start: Position, val end: Position) {
  fun endIn(location: Location): Location {
    return copy(end = location.end)
  }
}

class Position {
  val line: Int
  val column: Int

  constructor(line: Int, column: Int) {
    this.line = line
    this.column = column
  }

  // Note that we suppress `ConvertSecondaryConstructorToPrimary` due to `return` expressions, so we
  // can still have the `line` and `column` properties immutable.
  @Suppress("ConvertSecondaryConstructorToPrimary")
  constructor(position: Int, file: File) {
    var lineNumber = 0
    var charPosition = 0
    for (line in file.readLines()) {
      lineNumber++
      var columnNumber = 0
      for (column in line) {
        charPosition++
        columnNumber++

        if (charPosition == position) {
          this.line = lineNumber
          this.column = columnNumber
          return
        }
      }
      charPosition++
      if (charPosition == position) {
        this.line = lineNumber
        this.column = columnNumber
        return
      }
    }
    this.line = -1
    this.column = -1
  }
}
