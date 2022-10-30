package ekko.typing

import ekko.typing.tree.Type
import ekko.typing.tree.apply

typealias Substitution = Map<String, Type>

fun emptySubstitution(): Substitution = emptyMap()

fun substitutionOf(vararg pairs: Pair<String, Type>): Substitution = mapOf(*pairs)

infix fun Substitution.compose(other: Substitution): Substitution =
  plus(other).mapValues { (_, type) -> type apply this }
