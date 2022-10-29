package ekko.parsing.tree

sealed interface Lit {
  val location: Location

  data class Int(val value: kotlin.Int, override val location: Location) : Lit

  data class Float(val value: kotlin.Float, override val location: Location) : Lit

  data class String(val value: kotlin.String, override val location: Location) : Lit {
    override fun toString(): kotlin.String = "LString(value=\"$value\")"
  }

  class Unit(override val location: Location) : Lit {
    override fun toString(): kotlin.String = "()"
  }
}
