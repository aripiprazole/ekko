package ekko.tree

data class Alt(val name: Ident, val patterns: List<Pat>, val exp: Exp)
