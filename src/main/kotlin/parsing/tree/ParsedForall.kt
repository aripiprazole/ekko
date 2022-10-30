package ekko.parsing.tree

data class ParsedForall(val names: Set<Ident>, val type: ParsedType, val location: Location) {
  override fun toString(): String = when {
    names.isEmpty() -> "$type"
    else -> "∀ ${names.joinToString(" ") { "'$it" }}. $type"
  }
}
