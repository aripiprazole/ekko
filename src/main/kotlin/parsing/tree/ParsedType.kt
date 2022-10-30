package ekko.parsing.tree

sealed class ParsedType {
  data class Variable(val name: Ident) : ParsedType()

  data class Group(val value: ParsedType, val location: Location) : ParsedType()

  data class Application(
    val lhs: ParsedType,
    val rhs: ParsedType,
    val location: Location,
  ) : ParsedType()
}
