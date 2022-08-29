# Thinking about syntax

Our goal in Ekko project, is having a full haskell-like language, with imports, lambdas, pattern matching, and even type
classes. And, said that, we need to model our language.

## Table of contents

- [Thinking about Syntax](#thinking-about-syntax)
  - [Language Features](#language-features)
  - [Abstract Syntax Tree](#abstract-syntax-tree)
  - [Location](#location)

## Language features

```haskell
data Either a b = Left a | Right b

data Maybe a = Nothing | Just a

data Person = Person {
  name : String,
  age : Int
}

external
println : String -> IO ()

module Person where
  -- omit the function type
  default = Person {
    name : "Carlos",
    age : 19
  }

  sayHello : Person -> IO ()
  sayHello (Person name age) =
    println "Hello, I'm $name, and i'm $age years old"

  -- or matching:
  -- sayHello : Person -> IO ()
  -- sayHello = \case
  --  (Person name age) -> println "Hello, I'm $name, and i'm $age years old"
  --
  -- or matching the parameter:
  -- sayHello : Person -> IO ()
  -- sayHello person = case person of
  --  (Person name age) -> println "Hello, I'm $name, and i'm $age years old"

people : [Person]
people = [
  Person.default,
  Person.default { name = "João" },
  Person.default { name = "Maria" }
]

main : IO ()
main = do
  traverse $ (\x -> sayHello x) <$> people
  -- or passing the lambda reference
  -- traverse $ sayHello <$> people

  println "hello, world"
```

But this model, is a little complex to start with, because, there are many features, like `type classes`(the `traverse`
function),
`lambdas`, `pattern matching`, `enums`, etc... And this is going to be hard to implement at the first view, so we will
simplify it.

```haskell
data Person = Person {
  name : String,
  age : Int
}

main : ()
main =
  let person = Person "Carlos" 19 in
  println person
```

And with this simple model, we will be incrementing throughout this article. This model does not have the following
features:

- interpolation
- type classes(instances, etc...)
- enums
- pattern matching
- externals
- do notation/monads
- pureness

## Abstract Syntax Tree

The Abstract Syntax Tree(known briefly as AST) is a tree representation of the Syntax using data types. The initial AST
of Ekko project is:

Expression in the base of expressions in a programming language, which can be in Ekko's case, from literals(integers,
decimals, strings, unit) to function calls(known as `EApp`) and lambdas(that will not be implemented at this moment of
the article).

```kt
// Exp.kt
sealed interface Exp

data class ELet(val bindings: Map<Ident, Alt>, val value: Exp) : Exp
data class ELit(val lit: Lit) : Exp
data class EVar(val id: Ident) : Exp
data class EApp(val lhs: Exp, val rhs: Exp) : Exp
data class EGroup(val value: Exp) : Exp
```

And literals are representation of simple and primary values, like pairs, tuples, integers, decimals, strings, units.

```kt
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

`Ident` are identifiers in the source code, that represents a name in the source code, like in `var expressions`(
like `println`, `x`; var expressions are expressions that access a variable in the context).

```kt
// Ident.kt
data class Ident(val name: String, val displayName: String = name) {
  override fun toString(): String = "'$displayName"
}
```

...And `Alt` are alternatives in a function, or in let bindings, like: `let f x = x in f 10`. They have patterns as
parameters, to enable the pattern matching at call, like Haskell, Elixir also do, and have an expression as the "body",
because the language is going to be a pure functional language.

```kt
// Alt.kt
data class Alt(val id: Ident, val patterns: List<Pat>, val exp: Exp)
```

So, `Pat` are representations of patterns, that at this moment, will not be taken in-deep, to maintain the simplicity.
But currently have a representation of name identifiers.

```kt
// Pat.kt
sealed interface Pat

data class PVar(val id: Ident) : Pat
```

## Location

We can add a location data type to the AST, to keep track of the source code location of each element. The importance of
maintaining the location between the elements of the AST, are:

- readability
- error handling
- debugging (in the compiler development)
- breakpoints (in case of real debugging with something like `nvim-dap` in neovim or even the intellij debugger)

```kt
// Location.kt
data class Location(val start: Position, val end: Position)

class Position {
  val line: Int
  val column: Int

  // Note that we suppress `ConvertSecondaryConstructorToPrimary` due to `return` expressions, so we
  // can still have the `line` and `column` properties immutable.
  @Suppress("ConvertSecondaryConstructorToPrimary")
  constructor(position: Int, file: File) {
    var lineNumber = 0
    var charPosition = 0
    for (line in file.readLines()) {
      lineNumber++
      var columnNumber = 0
      for (column in line) {
        charPosition++
        columnNumber++

        if (charPosition == position) {
          this.line = lineNumber
          this.column = columnNumber
          return
        }
      }
      charPosition++
      if (charPosition == position) {
        this.line = lineNumber
        this.column = columnNumber
        return
      }
    }
    this.line = -1
    this.column = -1
  }
}
```

This is a snippet for finding the line and column of both of the `start` and the `end` of a `text range`(the `Location`
class)
