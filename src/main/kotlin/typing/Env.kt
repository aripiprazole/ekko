package ekko.typing

typealias Env = Map<String, Forall>

fun emptyEnv(): Env = emptyMap()

fun envOf(vararg pairs: Pair<String, Forall>): Env = mapOf(pairs = pairs)

fun Env.ftv(): Set<String> = values.flatMap { it.ftv() }.toSet()

fun Env.extendEnv(vararg pairs: Pair<String, Forall>): Env {
  return this + envOf(pairs = pairs)
}
