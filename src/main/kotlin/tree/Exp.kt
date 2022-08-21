package ekko.tree

sealed interface Exp

data class ELit(val literal: Lit) : Exp

data class EVar(val id: Ident) : Exp

data class EApp(val lhs: Exp, val rhs: Exp) : Exp

data class ELet(val names: Map<Ident, Exp>, val exp: Exp) : Exp
