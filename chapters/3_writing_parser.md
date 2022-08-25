# Writing parser

Parsing can be hard when trying to write a parser from scratch, even if it is a top-down parser, combinators, etc... So this article we are going to use `ANTLR`, that is a full-featured parser generator, that generates for differents targets(Java, C#, etc), so you can reutilize the parser in another projects.

The Kotlin target for ANTLR, does not have official support, so we are going to use [Strumenta's](https://strumenta.com/) [antlr-kotlin](https://github.com/Strumenta/antlr-kotlin). You can checkout [here](https://github.com/gabrielleeg1/ekko/blob/main/build.gradle.kts) project's buildscript configuration for `antlr-kotlin`.
