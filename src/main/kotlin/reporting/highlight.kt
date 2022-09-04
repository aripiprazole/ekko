package ekko.reporting

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import ekko.parsing.tree.Location
import java.io.File

private val terminal = Terminal()

fun File.highlight(location: Location, buildMessage: () -> Message) {
  val message = buildMessage()

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

  run {
    val letter = message.prefix.first()

    terminal.append {
      append(message.color("%s[%s%02x]: ".format(message.prefix, letter, message.code)))
      append(message.text)
    }
  }

  terminal.append {
    append(TextColors.gray(" --> "))
    append("$path:$line:$start")
  }
  lines.forEach { (num, content) ->
    terminal.append {
      append(TextColors.gray(" %${numLength}s | ".format(num)))
      append(content)
    }

    if (num == line) {
      val highlight = MutableList(content.length) { " " }.apply {
        for (j in start..end) {
          set(j, "^")
        }
      }

      terminal.append {
        append(TextColors.gray(" ${" ".repeat(numLength)} | "))
        append(highlight.joinToString(""))
      }
    }
  }
}

private inline fun Terminal.append(builder: StringBuilder.() -> Unit) {
  println(buildString(builder))
}
