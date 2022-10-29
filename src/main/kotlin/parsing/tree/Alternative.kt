package ekko.parsing.tree

data class Alternative(
  val id: Ident,
  val patterns: List<Pat>,
  val expression: Expression,
  val location: Location,
)
