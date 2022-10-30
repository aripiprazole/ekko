# Writing parser

Parsing can be hard when trying to write a parser from scratch, even if it is a top-down parser, combinators, etc... So
this article we are going to use `ANTLR`, that is a full-featured parser generator, that generates for differents
targets(Java, C#, etc), so you can reutilize the parser in another projects.

We have no official support for ANTLR in Kotlin, but we can use the Java runtime, so we can use the generated parser by
the Java's gradle plugin. The plugin classpath is builtin in the gradle dependency manager.

PS: We have an unofficial support, but the java is more mature and maintained, you can check out the Strumenta's
runtime [here](https://github.com/Strumenta/antlr-kotlin)

## Table of contents

- [Writing parser](#writing-parser)
  - [Abstract Syntax Tree](#abstract-syntax-tree)
  - [Location](#location)
  - [Using ANTLR for Grammar](#using-antlr-for-grammar)
    - [Lexing](#lexing)
    - [Parsing](#parsing)
    - [Debugging](#debugging)
  - [Mapping tree to the AST](#mapping-tree-to-the-ast)
    - [Notes for mapping](#notes-for-mapping)

## Abstract Syntax Tree

The Abstract Syntax Tree(known briefly as AST) is a tree representation of the Syntax using data types. The initial AST
of Ekko project is:

Expression in the base of expressions in a programming language, which can be in Ekko's case, from literals(integers,
decimals, strings, unit) to function calls(known as `Expression.Application`) and lambdas(that will not be implemented at this moment of
the article).

```kotlin
// Expression.kt
sealed interface Expression {
  data class Let(val bindings: Map<Ident, Alternative>, val value: Expression) : Expression
  data class Literal(val lit: ekko.parsing.Literal) : Expression
  data class Variable(val id: Ident) : Expression
  data class Application(val lhs: Expression, val rhs: Expression) : Expression
  data class Group(val value: Expression) : Expression
}
```

And literals are representation of simple and primary values, like pairs, tuples, integers, decimals, strings, units.

```kotlin
// Litteral.kt
sealed interface Literal {
  data class Int(val value: kotlin.Int) : Lit
  data class Float(val value: kotlin.Float) : Lit
  data class String(val value: kotlin.String) : Lit {
    override fun toString(): kotlin.String = "String(value=\"$value\")"
  }

  object Unit : Literal {
    override fun toString(): kotlin.String = "()"
  }
}
```

`Ident` are identifiers in the source code, that represents a name in the source code, like in `var expressions`(
like `println`, `x`; var expressions are expressions that access a variable in the context).

```kotlin
// Ident.kt
data class Ident(val name: String, val displayName: String = name) {
  override fun toString(): String = "'$displayName"
}
```

...And `Alternative` are alternatives in a function, or in let bindings, like: `let f x = x in f 10`. They have patterns
as
parameters, to enable the pattern matching at call, like Haskell, Elixir also do, and have an expression as the "body",
because the language is going to be a pure functional language.

```kotlin
// Alt.kt
data class Alternative(val id: Ident, val patterns: List<Pat>, val expression: Expression)
```

So, `Pattern` are representations of patterns, that at this moment, will not be taken in-deep, to maintain the
simplicity.
But currently have a representation of name identifiers.

```kotlin
// Pattern.kt
sealed interface Pattern {
  data class Variable(val id: Ident) : Pattern
}
```

## Location

We can add a location data type to the AST, to keep track of the source code location of each element. The importance of
maintaining the location between the elements of the AST, are:

- readability
- error handling
- debugging (in the compiler development)
- breakpoints (in case of real debugging with something like `nvim-dap` in neovim or even the intellij debugger)

```kotlin
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

## Using ANTLR for Grammar

After setting up the generator, we can create a `.g4` file and start playing with it:

```antlr
grammar Ekko;
```

If you want to separate the lexer and the parser, you can create the `EkkoParser.g4` and `EkkoLexer.g4` files, and set
up the lexer tokens in parser's file with `tokenVocab` option:

```antlr
parser grammmar EkkoParser;

options {
  tokenVocab = EkkoLexer;
}
```

### Lexing

The lexers that we will be creating will ever be in `screaming snake case` by pattern. And for the rules, you can read
more about the lexer rules to implement your
language [here](https://github.com/antlr/antlr4/blob/master/doc/lexer-rules.md).

```antlr
NEWLINE: ([\r] | [\n])+;
WS: (' ' | '\t' | NEWLINE)+ -> channel(HIDDEN);
```

We will first of all, define the tokens that are junk and whitespace(in this case, all are whitespace). And now we will
define the constant tokens:

```antlr
LET: 'let';
IN: 'in';

LPAREN: '(';
RPAREN: ')';
EQ: '=';
COLON: ',';
BAR: '\\';
ARROW: '->';
```

Done that, we can think about the "variable" ones, like `identifiers`, `strings` and `numbers`, the rules that are going
to match what
the user writes, and not check the equality:

```antlr
IDENT: ['a-zA-Z_]['a-zA-Z0-9_]*;
STRING: '"' (~["\r\n\\] | '\\' ~[\r\n])* '"';
INT: [0-9]+ ;
DECIMAL: INT '.' INT;
```

In our case, an identifier can have any character from a to z(that can be uppercased), underscores, apostrophes, and
numbers, but can never start with a number. And the string, can interpolate the `\` character and have line breaks.

> PS: the order of the lexer rules matters in deciding that will be chosen by the generated lexer in "ambiguities" like
> with a rule like `IDENT` and a rule that matches `YES: yes`, if you put the `IDENT` after the `YES`, the `YES` will be
> chosen, and the `IDENT` will be ignored.

### Parsing

Parsing can be a hard thing, when you don't have a parser generator, but with ANTLR, you have left recursion and can do
things very simple for the parser. So let's start with a simple expression:

```antlr
exp: value=IDENT   # EVar
   | value=STRING  # EString
   | value=INT     # EInt
   | value=DECIMAL # EDecimal
   ;
```

Look that we can match values, and the generator get the things done for us. And the `value=` is the name of the
property that will be generated in the `antlr generated tree`.

We can match more lexer rules in a parser rule, like when adding the `group expression`:

```antlr
exp: value=IDENT             # EVar
   | value=STRING            # EString
   | value=INT               # EInt
   | value=DECIMAL           # EDecimal
   | LPAREN value=exp RPAREN # EGroup
   ;
```

> Never add a name for token rules that you will not use in the source code like with `LPAREN` and `RPAREN`, this will
> just be unused.

Now we can parse more complex expressions like `applications`(function calls) and `lambdas`:

```antlr
exp: ...
   | lhs=exp rhs=exp               # EApp
   | BAR param=pat ARROW value=exp # EAbs
   ;
```

And with this pattern, we can implement the `let expression` too, and combine more parser rules:

```antlr
pat: name=IDENT # PVar;

alt: name=IDENT pat* EQ value=exp;

exp: LET alt (COLON alt)* IN value=exp # ELet
   | ...
   ;
```

### Debugging

Debugging the antlr generated tree is hard without mapping, so we can
use [this snippet](https://github.com/gabrielleeg1/ekko/blob/main/src/main/kotlin/parser/ParseTree.kt) for pretty
printing a simplified version of the generated parse tree. For example:

```kotlin
val parser: EkkoParser

println(parser.exp().toParseTree().multilineString())
```

## Mapping tree to the AST

The generated code from the parser will never replace our AST, cause its dirty and uncontrolled/not versioned by git.
So we need to map the tree to the AST.

We can start making utilitary functions like:

```kotlin
// parserUtils.kt

fun ParserRuleContext.getLocationIn(file: File): Location {
  return Location(
    start = Position(start.startIndex, file),
    end = Position(stop.stopIndex, file),
  )
}

fun Token.treeToIdent(file: File): Ident {
  return Ident(
    text,
    location = Location(
      start = Position(startIndex, file),
      end = Position(stopIndex, file),
    ),
  )
}
```

This will make easier to deal with locations when mapping. So we can start by defining a function that
maps `expressions`:

```kotlin
// treeToExp.kt

fun ExpContext.treeToExpression(file: File): Exp {
  return when (this) {
    is ELetContext -> {
      val names = alt().map { it.treeToAlternative(file) }.associateBy { it.id }
      val value = value.treeToExpression(file)

      Expression.Let(names, value, getLocationIn(file))
    }

    is EStringContext -> {
      // We use `.substring()` here, because the lexer includes the " characters at the start and at
      // the end of the string.
      val text = value.text.substring(1, value!!.text!!.length - 1)

      Expression.Literal(Literal.String(text, getLocationIn(file)))
    }

    is EAppContext -> {
      Exprssion.Application(lhs.treeToExpression(file), rhs.treeToExpression(file), getLocationIn(file))
    }

    // ...
  }
}
```

> The entire source code of mappers described in this article is
> located [here](https://github.com/gabrielleeg1/ekko/tree/main/src/main/kotlin/parser).

We will need to make this job for all the other rules of the parser, walking throughout all the antlr tree, so we can
get the new mapped tree.

### Notes for mapping

With the Kotlin 1.7 update, we can use `context receivers` with a specific compiler flag, and this will make the code
cleaner like:

```kotlin
context(File)
fun ParserRuleContext.currentLocation(): Location {
  return Location(Position(start.startIndex, this@File), Position(stop.stopIndex, this@File))
}

context(File)
fun ExpContext.treeToExpression(): Expression {
  return when (this) {
    is ELetContext -> {
      val names = alt().map { it.treeToAlternative() }.associateBy { it.id }
      val value = value.treeToExp()

      Expression.Let(names, value, currentLocation())
    }

    // ...
  }
}
```

But in this article, we are regarding the compatibility, so it will not be used here, because it is an experimental
feature in the kotlin project's version.
