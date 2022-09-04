package ekko.parsing.tree

sealed interface Lit {
  val location: Location
}

data class LInt(val value: Int, override val location: Location) : Lit

data class LFloat(val value: Float, override val location: Location) : Lit

data class LString(val value: String, override val location: Location) : Lit {
  override fun toString(): String = "LString(value=\"$value\")"
}

class LUnit(override val location: Location) : Lit {
  override fun toString(): String = "()"
}
