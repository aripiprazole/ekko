package ekko.parsing.tree

data class ParsedForall(val names: Set<Ident>, val type: ParsedType) {
  constructor(vararg names: Ident, builder: () -> ParsedType) : this(names.toSet(), builder())

  override fun toString(): String = when {
    names.isEmpty() -> "$type"
    else -> "∀ ${names.joinToString(" ") { "'$it" }}. $type"
  }
}
