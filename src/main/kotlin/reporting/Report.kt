package ekko.reporting

import ekko.parsing.tree.Location
import ekko.parsing.tree.Position
import java.io.File

class Report(val file: File) {
  private val messages: MutableList<Pair<Location, Message>> = mutableListOf()

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
        val letter = message.prefix.first()

        "%s[%s%02x]: %s".format(message.prefix, letter, message.code, message.text)
      }
    }
  }

  companion object {
    fun build(file: File, builder: Report.() -> Unit): Report {
      return Report(file).apply(builder)
    }
  }
}
