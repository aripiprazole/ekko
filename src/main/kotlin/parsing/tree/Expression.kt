package ekko.parsing.tree

import ekko.parsing.tree.Literal as AstLiteral

sealed interface Expression {
  val location: Location

  data class Literal(val lit: AstLiteral, override val location: Location = lit.location) :
    Expression

  data class Variable(val id: Ident, override val location: Location) : Expression

  data class Group(val value: Expression, override val location: Location) : Expression

  data class Abstraction(
    val param: Pat,
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
