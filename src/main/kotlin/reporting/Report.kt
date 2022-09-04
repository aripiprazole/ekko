package ekko.reporting

import ekko.parsing.tree.Location
import ekko.parsing.tree.Position
import java.io.File

class Report(val file: File) {
  private val messages: MutableList<Pair<Location, Message>> = mutableListOf()

  fun isEmpty(): Boolean {
    return messages.isEmpty()
  }

  fun isNotEmpty(): Boolean {
    return messages.isNotEmpty()
  }

  fun addMessage(range: IntRange, buildMessage: () -> Message) {
    val location = Location(Position(range.first, file), Position(range.last, file))

    messages.add(location to buildMessage())
  }

  fun addMessage(location: Location, buildMessage: () -> Message) {
    messages.add(location to buildMessage())
  }

  fun show() {
    messages.forEach { (location, message) ->
      file.highlight(location) {
        message
      }
    }
  }

  companion object {
    fun build(file: File, builder: Report.() -> Unit): Report {
      return Report(file).apply(builder)
    }
  }
}
