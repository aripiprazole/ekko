package ekko.typing

typealias Subst = Map<String, Typ>

fun emptySubst(): Subst = emptyMap()

fun substOf(vararg pairs: Pair<String, Typ>): Subst = mapOf(*pairs)

infix fun Subst.compose(other: Subst): Subst =
  plus(other).mapValues { (_, type) -> type apply this }
