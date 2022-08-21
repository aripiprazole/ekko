package ekko.tree

sealed interface Lit

data class LInt(val value: Int) : Lit

data class LFloat(val value: Float) : Lit

data class LString(val value: String) : Lit {
  override fun toString(): String = "LString(value=\"$value\")"
}

object LUnit : Lit {
  override fun toString(): String = "()"
}
