package ekko.parsing.tree

data class Alternative(
  val id: Ident,
  val patterns: List<Pat>,
  val exp: Exp,
  val location: Location,
)
