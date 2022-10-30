package ekko.typing

sealed interface Type {
  companion object {
    val Unit: Type = constructor("()")
    val Int: Type = constructor("Int")
    val Float: Type = constructor("Float")
    val String: Type = constructor("String")
    val Arrow: Type = constructor("->")

    fun variable(name: String): Type = Variable(name)
    fun constructor(name: String): Type = Constructor(name)
    fun app(lhs: Type, rhs: Type): Type = Application(lhs, rhs)
  }

  data class Application(val lhs: Type, val rhs: Type) : Type {
    override fun toString(): String = when {
      lhs is Application && rhs is Application -> "($lhs) $rhs"
      else -> "$lhs $rhs"
    }
  }

  data class Constructor(val id: String) : Type {
    override fun toString(): String = id
  }

  data class Variable(val id: String) : Type {
    override fun toString(): String = "'$id"
  }
}

fun Type.ftv(): Set<String> = when (this) {
  is Type.Constructor -> emptySet()
  is Type.Variable -> setOf(id)
  is Type.Application -> lhs.ftv() + rhs.ftv()
}

infix fun Type.apply(subst: Substitution): Type {
  return when (this) {
    is Type.Application -> copy(lhs = lhs apply subst, rhs = rhs apply subst)
    is Type.Constructor -> this
    is Type.Variable -> subst[id] ?: this
  }
}

infix fun Type.arrow(rhs: Type) = Type.Application(this, Type.Application(Type.Arrow, rhs))
