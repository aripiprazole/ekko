# Thinking about syntax

## Abstract Syntax Tree

The Abstract Syntax Tree(known briefily as AST) is a tree representation of the Syntax using data types. The initial AST of Ekko project is:

Expression in the base of expressions in a programming language, which can be in Ekko's case, from literals(integers, decimals, strings, unit) to function calls(known as `EApp`) and lambdas(that will not be implemented in this moment of the article).

```kotlin
// Exp.kt
sealed interface Exp

data class ELit(val lit: Lit) : Exp
data class EVar(val id: Ident) : Exp
data class EApp(val lhs: Exp, val rhs: Exp) : Exp
data class EGroup(val value: Exp) : Exp
```

And literals are representation of simple and primary values, like pairs, tuples, integers, decimals, strings, units.

```kotlin
// Lit.kt
sealed interface Lit

data class LInt(val value: Int) : Lit
data class LFloat(val value: Float) : Lit
data class LString(val value: String) : Lit {
  override fun toString(): String = "LString(value=\"$value\")"
}

object LUnit : Lit {
  override fun toString(): String = "()"
}
```

`Ident` are identifiers in the source code, that represents a name in the source code, like in `var expressions`(like `println`, `x`; var expressions are expressions that access a variable in the context).

```kotlin
// Ident.kt
data class Ident(val name: String, val displayName: String = name) {
  override fun toString(): String = "'$displayName"
}
```

...And `Alt` are alternatives in a function, or in let bindings, like: `let f x = x in f 10`. They have patterns as parameters, to enable the pattern matching at call, like Haskell, Elixir also do, and have an expression as the "body", because the language is going to be a pure functional language.

```kotlin
// Alt.kt
data class Alt(val id: Ident, val patterns: List<Pat>, val exp: Exp)
```

So, `Pat` are representations of patterns, that at this moment, will not be taken in-deep, to maintain the simplicity. But currently have a representation of name identifiers.

```kotlin
// Pat.kt
sealed interface Pat

data class PVar(val id: Ident) : Pat
```
