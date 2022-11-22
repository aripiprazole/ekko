package ekko.parsing.tree

data class ParsedModule(
  val name: Ident,
  val imports: Set<Import>,
  val declarations: Set<Declaration>,
  val location: Location,
)
