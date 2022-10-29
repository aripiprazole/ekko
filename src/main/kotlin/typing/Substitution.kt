package ekko.typing

typealias Substitution = Map<String, Typ>

fun emptySubstitution(): Substitution = emptyMap()

fun substOf(vararg pairs: Pair<String, Typ>): Substitution = mapOf(*pairs)

infix fun Substitution.compose(other: Substitution): Substitution =
  plus(other).mapValues { (_, type) -> type apply this }
