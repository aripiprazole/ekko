package ekko.tree

sealed interface Typ

data class TApp(val receiver: Typ, val arg: Typ) : Typ {
  override fun toString(): String = when {
    receiver is TApp && arg is TApp -> "($receiver) $arg"
    else -> "$receiver $arg"
  }
}

data class TCon(val id: String) : Typ {
  override fun toString(): String = id
}

data class TVar(val id: String) : Typ {
  override fun toString(): String = id
}
