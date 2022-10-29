package ekko.parsing.tree

import ekko.parsing.tree.Lit as AstLit

sealed interface Exp {
  val location: Location

  data class Lit(val lit: AstLit, override val location: Location = lit.location) : Exp

  data class Var(val id: Ident, override val location: Location) : Exp

  data class App(val lhs: Exp, val rhs: Exp, override val location: Location) : Exp

  data class Abs(val param: Pat, val value: Exp, override val location: Location) : Exp

  data class Group(val value: Exp, override val location: Location) : Exp

  data class Let(
    val bindings: Map<Ident, Alternative>,
    val value: Exp,
    override val location: Location,
  ) : Exp
}
