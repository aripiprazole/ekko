package ekko.parsing.tree

sealed interface ParsedType {
  val location: Location

  data class Variable(val name: Ident, override val location: Location = name.location) : ParsedType

  data class Group(val value: ParsedType, override val location: Location) : ParsedType

  data class Application(
    val lhs: ParsedType,
    val rhs: ParsedType,
    override val location: Location,
  ) : ParsedType
}
