package ekko.reporting

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import ekko.parsing.tree.Location
import java.io.File

private val terminal = Terminal()

fun File.highlight(location: Location, buildMessage: () -> Message) {
  return Highlight(buildMessage(), this, location).printHighlight()
}

private class Highlight(val message: Message, val file: File, val location: Location) {
  val line = location.start.line - 1
  val start = location.start.column
  val end = location.end.column

  val indexedLines = file.readLines().mapIndexed(Int::to)

  val lines = when {
    line == 0 && indexedLines.size == 1 -> indexedLines
    line == 0 && indexedLines.size > 2 -> indexedLines.subList(0, 1)
    line == indexedLines.size -> indexedLines.subList(line - 1, line + 1)
    else -> indexedLines
  }

  val maxLineNum = lines.maxOfOrNull { it.first }!!.toString()
  val numLength = maxLineNum.length

  fun printHighlight() {
    printMessage(message)
    printLocation(location)

    lines.forEach { (num, content) ->
      printLine(num, content)

      if (num == line) {
        printIndicator(content)
      }
    }
  }

  fun printMessage(message: Message) {
    val letter = message.prefix.uppercase().first()

    terminal.append {
      append(message.color("%s[%s%02x]: ".format(message.prefix, letter, message.code)))
      append(message.text)
    }
  }

  fun printLocation(location: Location) {
    terminal.append {
      append(TextColors.gray(" --> "))
      append("${file.path}:${location.start.line}:${location.start.column}")
    }
  }

  fun printLine(num: Int, content: String) {
    terminal.append {
      append(TextColors.gray(" %${numLength}s | ".format(num)))
      append(content)
    }
  }

  fun printIndicator(content: String) {
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

  inline fun Terminal.append(builder: StringBuilder.() -> Unit) {
    println(buildString(builder))
  }
}
