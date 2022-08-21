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
  - [Getting started](#getting-started)
    - [Yeoman](#yeoman)
    - [IntelliJ](#intellij)
  - [Parsing](#parsing)
    - [Abstract Syntax Tree](#abstract-syntax-tree)
    - [ANTLR Kotlin](#antlr-kotlin)
      - [Creating gradle task](#creating-gradle-task)
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

### Yeoman

You can bootstrap the gradle project using [yeoman](https://yeoman.io/)
and [gradle-kotlin plugin](https://github.com/jcdenton/generator-gradle-kotlin).

```bash
yo gradle-kotlin
```

### IntelliJ

You can use the default project wizard to create a new project.

<img src="assets/intellij-wizard.png" alt="IntelliJ Project Wizard">

## Parsing

### Abstract Syntax Tree

The Abstract Syntax Tree(AST) is a tree representation of the Syntax using data types.

The initial AST of Ekko project is:

> Exp.kt

```kotlin
sealed interface Exp

data class ELit(val lit: Lit) : Exp

data class EVar(val id: Ident) : Exp

data class EApp(val lhs: Exp, val rhs: Exp) : Exp

data class EGroup(val value: Exp) : Exp
```

> Lit.kt

```kotlin
sealed interface Lit

data class LInt(val value: Int) : Lit

data class LFloat(val value: Float) : Lit

data class LString(val value: String) : Lit {
  override fun toString(): String = "LStr(value=\"$value\")"
}

object LUnit : Lit {
  override fun toString(): String = "()"
}
```

> Ident.kt

```kotlin
data class Ident(val name: String, val displayName: String = name) {
  override fun toString(): String = "'$displayName"
}
```

> Alt.kt

```kotlin
data class Alt(val id: Ident, val patterns: List<Pat>, val exp: Exp)
```

> Pat.kt

```kotlin
sealed interface Pat

data class PVar(val id: Ident) : Pat
```

### ANTLR Kotlin

Setting up the [Strumenta](https://strumenta.com/)'s [antlr-kotlin](https://github.com/Strumenta/antlr-kotlin).

We will use the legacy mode for the plugin, because it only exposes a Task.

> build.gradle.kts

```kt
buildscript {
  repositories {
    maven("https://jitpack.io")
    mavenCentral()
  }

  dependencies {
    classpath("com.strumenta.antlr-kotlin:antlr-kotlin-gradle-plugin:$antlr_kotlin_version")
  }
}
```

> PS: Lookup the ANTLR Kotlin version at [JitPack](https://jitpack.io/#com.strumenta.antlr-kotlin/antlr-kotlin).

The project will need to include the generated source code in the `main source set`.

> build.gradle.kts

```kotlin
kotlin {
  sourceSets {
    main {
      kotlin.srcDirs(rootProject.file("src/generated/kotlin"))
    }
  }
}
```

#### Creating gradle task

The project will need a gradle task to generate the parser grammar.

Antlr-Kotlin generates source code with many warnings, with the Kotlin's compiler, Detekt, Ktlint, etc. so will need two tasks:

> build.gradle.kts

```kotlin
val generateMainAntlrSource by tasks.creating(AntlrKotlinTask::class) {
  val dependencies = project.dependencies

  antlrClasspath = configurations.detachedConfiguration(
    dependencies.create("org.antlr:antlr4:4.7.1"),
    dependencies.create("com.strumenta.antlr-kotlin:antlr-kotlin-target:$antlr_kotlin_version"),
  )
  maxHeapSize = "64m"
  arguments = listOf("-package", "ekko.parser")
  source = project.objects
    .sourceDirectorySet("antlr", "antlr")
    .srcDir("src/main/antlr").apply {
      include("*.g4")
    }
  outputDirectory = buildDir.resolve("build/generated/kotlin")
}
```

And the task that will suppress the warnings as a [workaround].

> build.gradle.kts

```kt
val generateAntlrSource by tasks.creating(Copy::class) {
  dependsOn(generateMainAntlrSource)
  from(buildDir.resolve("build/generated/kotlin"))
  include("**/*.kt")
  filter<KtSuppressFilterReader>()
  into(rootProject.file("src/generated/kotlin"))
}
```

> KtSuppressFilterReader.kt

```kt
class KtSuppressFilterReader(
  reader: Reader,
) : FilterReader(StringReader(SUPPRESS_ANNOT_HEADER + reader.readText())) {
  companion object {
    private val SUPPRESS_ANNOT_HEADER = arrayOf(
      "UNNECESSARY_NOT_NULL_ASSERTION", "UNUSED_PARAMETER", "USELESS_CAST", "UNUSED_VALUE", "VARIABLE_WITH_REDUNDANT_INITIALIZER", "PARAMETER_NAME_CHANGED_ON_OVERRIDE", "SENSELESS_COMPARISON", "UNCHECKED_CAST", "UNUSED", "RemoveRedundantQualifierName", "RedundantCompanionReference", "RedundantVisibilityModifier", "FunctionName", "SpellCheckingInspection", "RedundantExplicitType", "ConvertSecondaryConstructorToPrimary", "ConstantConditionIf", "CanBeVal", "LocalVariableName", "RemoveEmptySecondaryConstructorBody", "LiftReturnOrAssignment", "MemberVisibilityCanBePrivate", "RedundantNullableReturnType", "OverridingDeprecatedMember", "EnumEntryName", "RemoveExplicitTypeArguments", "PrivatePropertyName", "ProtectedInFinal", "MoveLambdaOutsideParentheses", "UnnecessaryImport", "KotlinRedundantDiagnosticSuppress", "ClassName", "CanBeParameter", "Detekt.MaximumLineLength", "Detekt.MaxLineLength", "Detekt.FinalNewline", "ktlint",
    ).joinToString(separator = ", ", prefix = "@file:Suppress(", postfix = ")\n\n") { "\"$it\"" }
  }
}

```

The `KtSuppressFilterReader` class you can put into the gradle build script, or use something like `composite builds` or `buildSrc`.

After this, make the `compileKotlin` task depends on `generateAntlrSource`.

```kotlin
tasks.withType<KotlinCompile> {
  dependsOn(generateAntlrSource)
}
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
