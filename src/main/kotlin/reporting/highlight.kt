package ekko.reporting

import ekko.parsing.tree.Location
import java.io.File

fun File.highlight(location: Location, buildMessage: () -> String) {
  val message: String = buildMessage()

  val line = location.start.line - 1
  val start = location.start.column
  val end = location.end.column

  val indexedLines = readLines().mapIndexed(Int::to)

  val lines = when {
    line == 0 && indexedLines.size == 1 -> indexedLines
    line == 0 && indexedLines.size > 2 -> indexedLines.subList(0, 1)
    line == indexedLines.size -> indexedLines.subList(line - 1, line + 1)
    else -> indexedLines
  }

  val maxLineNum = lines.maxOfOrNull { it.first }!!.toString()
  val numLength = maxLineNum.length

  println(message)

  println(" ==> $path:$line:$start")
  lines.forEach { (num, content) ->
    println(" %${numLength}s | $content".format(num))

    if (num == line) {
      val highlight = MutableList(content.length) { " " }.apply {
        for (j in start..end) {
          set(j, "^")
        }
      }

      println(" ${" ".repeat(numLength)} | ${highlight.joinToString("")}")
    }
  }
}
