package ekko.typing

data class Forall(val names: Set<String>, val type: Type) {
  constructor(vararg names: String, builder: () -> Type) : this(names.toSet(), builder())

  override fun toString(): String = when {
    names.isEmpty() -> "$type"
    else -> "∀ ${names.joinToString(" ") { "'$it" }}. $type"
  }
}

fun Forall.apply(subst: Substitution): Forall {
  return Forall(names, type.apply(subst))
}

fun Forall.ftv(): Set<String> {
  return type.ftv().filter { it !in names }.toSet()
}
