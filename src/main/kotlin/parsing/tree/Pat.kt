package ekko.parsing.tree

sealed interface Pat {
  data class Variable(val id: Ident, val location: Location = id.location) : Pat
}

// data class PAs(val id: Ident, val pat: Pat) : Pat
//
// data class PLit(val lit: Lit) : Pat
//
// data class PCon(val id: Ident, val pats: List<Pat>) : Pat
//
// object PWildcard : Pat {
//  override fun toString(): String = "PWildcard"
// }
