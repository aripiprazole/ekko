package ekko.reporting

import com.github.ajalt.mordant.rendering.TextColors

sealed class Message(
  val code: Int,
  val name: String,
  val text: String,
) {
  abstract val color: TextColors
  abstract val prefix: String

  abstract class Hint(code: Int, name: String, message: String) : Message(code, name, message) {
    override val color: TextColors = TextColors.brightBlue
    override val prefix: String = "hint"
  }

  abstract class Warning(code: Int, name: String, message: String) : Message(code, name, message) {
    override val color: TextColors = TextColors.brightYellow
    override val prefix: String = "warning"
  }

  abstract class Error(code: Int, name: String, message: String) : Message(code, name, message) {
    override val color: TextColors = TextColors.brightRed
    override val prefix: String = "error"
  }
}
