package ekko.reporting

sealed class Message(
  val code: Int,
  val name: String,
  val text: String,
) {
  abstract val prefix: String
}

abstract class MHint(code: Int, name: String, message: String) : Message(code, name, message) {
  override val prefix: String = "Hint"
}

abstract class MWarning(code: Int, name: String, message: String) : Message(code, name, message) {
  override val prefix: String = "Warning"
}

abstract class MError(code: Int, name: String, message: String) : Message(code, name, message) {
  override val prefix: String = "Error"
}
