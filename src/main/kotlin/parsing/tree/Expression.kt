package ekko.parsing.tree

import ekko.parsing.tree.Lit as AstLit

sealed interface Expression {
  val location: Location

  data class Lit(val lit: AstLit, override val location: Location = lit.location) : Expression

  data class Variable(val id: Ident, override val location: Location) : Expression

  data class App(val lhs: Expression, val rhs: Expression, override val location: Location) : Expression

  data class Abs(val param: Pat, val value: Expression, override val location: Location) : Expression

  data class Group(val value: Expression, override val location: Location) : Expression

  data class Let(
    val bindings: Map<Ident, Alternative>,
    val value: Expression,
    override val location: Location,
  ) : Expression
}
