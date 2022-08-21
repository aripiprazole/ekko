package ekko.typing

sealed interface Typ {
  companion object {
    val Unit: Typ = constructor("()")
    val Int: Typ = constructor("Int")
    val Float: Typ = constructor("Float")
    val Str: Typ = constructor("Str")
    val Arrow: Typ = constructor("->")

    fun variable(name: String): Typ = TVar(name)
    fun constructor(name: String): Typ = TCon(name)
    fun app(lhs: Typ, rhs: Typ): Typ = TApp(lhs, rhs)
  }
}

data class TApp(val lhs: Typ, val rhs: Typ) : Typ {
  override fun toString(): String = when {
    lhs is TApp && rhs is TApp -> "($lhs) $rhs"
    else -> "$lhs $rhs"
  }
}

data class TCon(val id: String) : Typ {
  override fun toString(): String = id
}

data class TVar(val id: String) : Typ {
  override fun toString(): String = "'$id"
}

fun Typ.ftv(): Set<String> = when (this) {
  is TCon -> emptySet()
  is TVar -> setOf(id)
  is TApp -> lhs.ftv() + rhs.ftv()
}

infix fun Typ.apply(subst: Subst): Typ {
  return when (this) {
    is TApp -> copy(lhs = lhs apply subst, rhs = rhs apply subst)
    is TCon -> this
    is TVar -> subst[id] ?: this
  }
}

infix fun Typ.arrow(rhs: Typ) = TApp(this, TApp(Typ.Arrow, rhs))
