package ekko.tree

sealed interface Lit

data class LInt(val value: Int) : Lit

data class LFloat(val value: Float) : Lit

object LUnit : Lit {
  override fun toString(): String = "()"
}
