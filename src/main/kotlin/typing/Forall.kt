package ekko.typing

data class Forall(val names: Set<String>, val typ: Typ) {
  constructor(vararg names: String, builder: () -> Typ) : this(names.toSet(), builder())

  override fun toString(): String = when {
    names.isEmpty() -> "$typ"
    else -> "∀ ${names.joinToString(" ") { "'$it" }}. $typ"
  }
}

fun Forall.apply(subst: Substitution): Forall {
  return Forall(names, typ.apply(subst))
}

fun Forall.ftv(): Set<String> {
  return typ.ftv().filter { it !in names }.toSet()
}
