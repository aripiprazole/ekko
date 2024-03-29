package ekko.parsing.tree

import ekko.parsing.tree.Literal as AstLiteral

sealed interface Expression {
  val location: Location

  data class Literal(val literal: AstLiteral, override val location: Location = literal.location) :
    Expression

  data class Variable(val id: Ident, override val location: Location) : Expression

  data class Group(val value: Expression, override val location: Location) : Expression

  data class Abstraction(
    val parameter: Pattern,
    val value: Expression,
    override val location: Location,
  ) : Expression

  data class Application(
    val lhs: Expression,
    val rhs: Expression,
    override val location: Location,
  ) : Expression

  data class Let(
    val bindings: Map<Ident, Alternative>,
    val value: Expression,
    override val location: Location,
  ) : Expression
}
