package ekko.typing

data class Forall(val names: Set<String>, val typ: Typ) {
  override fun toString(): String = when {
    names.isEmpty() -> "$typ"
    else -> "∀ ${names.joinToString(" ") { "'$it" }}. $typ"
  }
}

fun Forall.ftv(): Set<String> {
  return typ.ftv().filter { it !in names }.toSet()
}
