package ekko.typing

sealed interface Type {
  companion object {
    val Unit: Type = constructor("()")
    val Int: Type = constructor("Int")
    val Float: Type = constructor("Float")
    val String: Type = constructor("String")
    val Arrow: Type = constructor("->")

    fun variable(name: String): Type = VarType(name)
    fun constructor(name: String): Type = ConstructorType(name)
    fun app(lhs: Type, rhs: Type): Type = AppType(lhs, rhs)
  }
}

data class AppType(val lhs: Type, val rhs: Type) : Type {
  override fun toString(): String = when {
    lhs is AppType && rhs is AppType -> "($lhs) $rhs"
    else -> "$lhs $rhs"
  }
}

data class ConstructorType(val id: String) : Type {
  override fun toString(): String = id
}

data class VarType(val id: String) : Type {
  override fun toString(): String = "'$id"
}

fun Type.ftv(): Set<String> = when (this) {
  is ConstructorType -> emptySet()
  is VarType -> setOf(id)
  is AppType -> lhs.ftv() + rhs.ftv()
}

infix fun Type.apply(subst: Substitution): Type {
  return when (this) {
    is AppType -> copy(lhs = lhs apply subst, rhs = rhs apply subst)
    is ConstructorType -> this
    is VarType -> subst[id] ?: this
  }
}

infix fun Type.arrow(rhs: Type) = AppType(this, AppType(Type.Arrow, rhs))
