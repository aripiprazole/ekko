package ekko.tree

sealed interface Exp {
  val location: Location
}

data class ELit(
  val lit: Lit,
  override val location: Location = lit.location,
) : Exp

data class EVar(
  val id: Ident,
  override val location: Location,
) : Exp

data class EApp(
  val lhs: Exp,
  val rhs: Exp,
  override val location: Location,
) : Exp

data class EAbs(
  val param: Pat,
  val value: Exp,
  override val location: Location,
) : Exp

data class EGroup(
  val value: Exp,
  override val location: Location,
) : Exp

data class ELet(
  val bindings: Map<Ident, Alt>,
  val value: Exp,
  override val location: Location,
) : Exp
