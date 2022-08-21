# Writing Haskell in Kotlin

This article talks about implementing a Haskell-like interpreter in Kotlin. That comes from
writing the parser, type system, context resolving to the interpreter.

The goal of this article, is to show a short introduction to compilers, interpreters, and
type systems. You can read more [here](#see-also).

This is not production-ready and not scientific material, but it is a good starting point for
learning.

## Table of contents

- [Writing Haskell in Kotlin](#writing-haskell-in-kotlin)
  - [Table of contents](#table-of-contents)
  - [What will be used](#what-will-be-used)
  - [Parsing](#parsing)
    - [Abstract Syntax Tree](#abstract-syntax-tree)
    - [ANTLR](#antlr)
    - [Mapping](#mapping)
    - [Pretty printing](#pretty-printing)
  - [Resolving](#resolving)
    - [Name resolving](#name-resolving)
    - [Import resolving](#import-resolving)
    - [Validating](#validating)
    - [Resolved Tree](#resolved-tree)
  - [Type System](#type-system)
    - [Algorithm W](#algorithm-w)
    - [Mutable substitutions](#mutable-substitutions)
    - [Mutable references](#mutable-references)
    - [Elaborating](#elaborating)
    - [Type classes](#type-classes)
  - [Evaluating](#evaluating)
  - [See also](#see-also)
  - [Bibliography](#bibliography)

## What will be used

- A Text Editor ([Visual Studio Code](https://code.visualstudio.com/), [IntelliJ](https://www.jetbrains.com/idea/),
  etc...)
- [ANTLR 4.7.1](https://www.antlr.org/)
- [Kotlin 1.7.10+](https://kotlinlang.org/)
- [Gradle 7.3.3+](https://gradle.org/)

## Getting started

You will need to bootstrap the gradle project.

## Parsing

Parsing can be hard when trying to write a parser from scratch, even if it is a top-down parser, combinators, etc... So this article we are going to use `ANTLR`, that is a full-featured parser generator, that generates for differents targets(Java, C#, etc), so you can reutilize the parser in another projects.

The Kotlin target for ANTLR, does not have official support, so we are going to use [Strumenta's](https://strumenta.com/) [antlr-kotlin](https://github.com/Strumenta/antlr-kotlin). You can checkout [here](https://github.com/gabrielleeg1/ekko/blob/main/build.gradle.kts) project's buildscript configuration for `antlr-kotlin`.

### Abstract Syntax Tree

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

### ANTLR

### Mapping

### Pretty printing

## Resolving

### Name resolving

### Import resolving

### Validating

### Resolved Tree

## Type System

The type system that we will use is
the [Hindley Milner](https://en.wikipedia.org/wiki/Hindley%E2%80%93Milner_type_system)(also known as Damas-Milner or
Damas-Hindley-Milner).
We will also need an inference algorithm,
the [Algorithm W](https://en.wikipedia.org/wiki/Hindley%E2%80%93Milner_type_system#Algorithm_W)

### Algorithm W

### Mutable substitutions

### Mutable references

### Elaborating

### Type classes

## Evaluating

## See also

## Bibliography

- https://smunix.github.io/dev.stephendiehl.com/fun/index.html
- http://web.cecs.pdx.edu/~mpj/thih/thih.pdf
- https://tomassetti.me/building-and-testing-a-parser-with-antlr-and-kotlin/
- https://tomassetti.me/building-advanced-parsers-using-kolasu/
- https://en.wikipedia.org/wiki/Abstract_syntax_tree
- https://en.wikipedia.org/wiki/Hindley%E2%80%93Milner_type_system#Algorithm_W
