# Writing parser

Parsing can be hard when trying to write a parser from scratch, even if it is a top-down parser, combinators, etc... So
this article we are going to use `ANTLR`, that is a full-featured parser generator, that generates for differents
targets(Java, C#, etc), so you can reutilize the parser in another projects.

The Kotlin target for ANTLR, does not have official support, so we are going to
use [Strumenta's](https://strumenta.com/) [antlr-kotlin](https://github.com/Strumenta/antlr-kotlin). You can
check out [here](https://github.com/gabrielleeg1/ekko/blob/main/build.gradle.kts) project's buildscript configuration
for `antlr-kotlin`.

## Table of contents

- [Writing parser](#writing-parser)
  - [Using ANTLR for Grammar](#using-antlr-for-grammar)
    - [Lexing](#lexing)
    - [Parsing](#parsing)
    - [Debugging](#debugging)
  - [Mapping tree to the AST](#mapping-tree-to-the-ast)
    - [Notes for mapping](#notes-for-mapping)

## Using ANTLR for Grammar

After setting up the generator, we can create a `.g4` file and start playing with it:

```antlrv4
grammar Ekko;
```

If you want to separate the lexer and the parser, you can create the `EkkoParser.g4` and `EkkoLexer.g4` files, and set
up the lexer tokens in parser's file with `tokenVocab` option:

```antlrv4
parser grammmar EkkoParser;

options {
  tokenVocab = EkkoLexer;
}
```

### Lexing

The lexers that we will be creating will ever be in `screaming snake case` by pattern. And for the rules, you can read
more about the lexer rules to implement your
language [here](https://github.com/antlr/antlr4/blob/master/doc/lexer-rules.md).

```antlrv4
NEWLINE: ([\r] | [\n])+;
WS: (' ' | '\t' | NEWLINE)+ -> channel(HIDDEN);
```

We will first of all, define the tokens that are junk and whitespace(in this case, all are whitespace). And now we will
define the constant tokens:

```antlrv4
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

```antlrv4
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

```antlrv4
exp: value=IDENT   # EVar
   | value=STRING  # EString
   | value=INT     # EInt
   | value=DECIMAL # EDecimal
   ;
```

Look that we can match values, and the generator get the things done for us. And the `value=` is the name of the
property that will be generated in the `antlr generated tree`.

We can match more lexer rules in a parser rule, like when adding the `group expression`:

```antlrv4
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

```antlrv4
exp: ...
   | lhs=exp rhs=exp               # EApp
   | BAR param=pat ARROW value=exp # EAbs
   ;
```

And with this pattern, we can implement the `let expression` too, and combine more parser rules:

```antlrv4
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
    start = Position(start!!.startIndex, file),
    end = Position(stop!!.stopIndex, file),
  )
}

fun Token.treeToIdent(file: File): Ident {
  return Ident(
    text!!,
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

// The properties generated with `antlr-kotlin` are always nullable, so we need to coerce that variables that we know
// aren't null.
fun ExpContext.treeToExp(file: File): Exp {
  return when (this) {
    is ELetContext -> {
      val names = findAlt().map { it.treeToAlt(file) }.associateBy { it.id }
      val value = value!!.treeToExp(file)

      ELet(names, value, getLocationIn(file))
    }

    is EAppContext -> {
      val lhs = lhs!!.treeToExp(file)
      val rhs = rhs!!.treeToExp(file)

      EApp(lhs, rhs, getLocationIn(file))
    }

    is EStringContext -> {
      // We use `.substring()` here, because the lexer includes the " characters at the start and at
      // the end of the string.
      val text = value!!.text!!.substring(1, value!!.text!!.length - 1)

      ELit(LString(text, getLocationIn(file)))
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
  return Location(Position(start!!.startIndex, this@File), Position(stop!!.stopIndex, this@File))
}

context(File)
fun ExpContext.treeToExp(): Exp {
  return when (this) {
    is ELetContext -> {
      val names = findAlt().map { it.treeToAlt() }.associateBy { it.id }
      val value = value!!.treeToExp()

      ELet(names, value, currentLocation())
    }

    // ...
  }
}
```

But in this article, we are regarding the compatibility, so it will not be used here, because it is an experimental
feature in the kotlin project's version.
