package ekko.typing

sealed interface Typ {
  companion object {
    val Unit: Typ = constructor("()")
    val Int: Typ = constructor("Int")
    val Float: Typ = constructor("Float")
    val String: Typ = constructor("String")
    val Arrow: Typ = constructor("->")

    fun variable(name: String): Typ = VarTyp(name)
    fun constructor(name: String): Typ = ConstructorTyp(name)
    fun app(lhs: Typ, rhs: Typ): Typ = AppTyp(lhs, rhs)
  }
}

data class AppTyp(val lhs: Typ, val rhs: Typ) : Typ {
  override fun toString(): String = when {
    lhs is AppTyp && rhs is AppTyp -> "($lhs) $rhs"
    else -> "$lhs $rhs"
  }
}

data class ConstructorTyp(val id: String) : Typ {
  override fun toString(): String = id
}

data class VarTyp(val id: String) : Typ {
  override fun toString(): String = "'$id"
}

fun Typ.ftv(): Set<String> = when (this) {
  is ConstructorTyp -> emptySet()
  is VarTyp -> setOf(id)
  is AppTyp -> lhs.ftv() + rhs.ftv()
}

infix fun Typ.apply(subst: Substitution): Typ {
  return when (this) {
    is AppTyp -> copy(lhs = lhs apply subst, rhs = rhs apply subst)
    is ConstructorTyp -> this
    is VarTyp -> subst[id] ?: this
  }
}

infix fun Typ.arrow(rhs: Typ) = AppTyp(this, AppTyp(Typ.Arrow, rhs))
