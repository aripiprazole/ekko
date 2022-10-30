package ekko.parsing.tree

sealed interface Literal {
  val location: Location

  data class Int(val value: kotlin.Int, override val location: Location) : Literal

  data class Float(val value: kotlin.Float, override val location: Location) : Literal

  data class String(val value: kotlin.String, override val location: Location) : Literal {
    override fun toString(): kotlin.String = "LString(value=\"$value\")"
  }

  class Unit(override val location: Location) : Literal {
    override fun toString(): kotlin.String = "()"
  }
}
