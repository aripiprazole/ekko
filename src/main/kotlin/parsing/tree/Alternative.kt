package ekko.parsing.tree

data class Alternative(
  val id: Ident,
  val patterns: List<Pattern>,
  val expression: Expression,
  val location: Location,
)
