package ekko.tree

data class Ident(val name: String, val displayName: String = name) {
  override fun toString(): String = "'$displayName"
}
