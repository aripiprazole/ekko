package ekko.tree

sealed interface Exp

data class ELit(val literal: Lit) : Exp

data class EVar(val id: Ident) : Exp

data class ECall(val callee: Exp, val arg: Exp) : Exp

data class ELet(val declarations: Map<Ident, Alt>, val exp: Exp) : Exp
