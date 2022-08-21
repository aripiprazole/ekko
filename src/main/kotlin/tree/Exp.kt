package ekko.tree

sealed interface Exp

data class ELit(val lit: Lit) : Exp

data class EVar(val id: Ident) : Exp

data class EApp(val lhs: Exp, val rhs: Exp) : Exp

data class EGroup(val value: Exp) : Exp

data class ELet(val bindings: Map<Ident, Alt>, val value: Exp) : Exp
