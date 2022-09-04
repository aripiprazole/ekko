package ekko.parsing.tree

data class Ident(val name: String, val displayName: String = name, val location: Location) {
  override fun toString(): String = "'$displayName"
}
