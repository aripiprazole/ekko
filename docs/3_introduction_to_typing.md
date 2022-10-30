# Introduction to Typing

Typing is an important feature of a programming language, like in `Rust`, and make possible to write safer code.
Our `ekko` language is going to be a `statically typed language`, this means that we can't have dynamic values like in
`JavaScript`, `Python`, `PHP`.

## Table of content

- [Introduction to Typing](#introduction-to-typing)
  - [Theory](#theory)
  - [Hindley Milner](#hindley-milner)
    - [Expressions](#expressions)
    - [Types](#types)
    - [Unification and free variables](#unification-and-free-variables)
    - [Generalization and Instantiation](#generalization-and-instantiation)
    - [Infer](#infer)

## Theory

Types are kinds of `sets` or `constraints` of possible values, like
in [set theory](https://en.wikipedia.org/wiki/Set_theory). We can have inference rules like:

$$ {x:\sigma \in \Gamma} \over {\Gamma \vdash x:\sigma} $$

That means, if we know that $x$ have a type $\sigma$ in context $\Gamma$, so $x$ have a type $sigma$.

## Hindley Milner

The Ekko's base type system is going to be the [Hindley Milner](https://en.wikipedia.org/wiki/Hindley%E2%80%93Milner_type_system). And it can be described with the syntax rules previously described.

### Expressions

The expressions, that are based in lambda-calculus, extended with a let-expression. That can be represented with out previously written abstract syntax tree.

$$
\begin{array}{lrll}
  e & =     & x                                   & \textrm{EVar} \\
    & \vert & e_1\ e_2                            & \textrm{EApp} \\
    & \vert & \lambda\ x\ .\ e                    & \textrm{EAbs} \\
    & \vert & \mathtt{let}\ x = e_1\ \mathtt{in}\ e_2 & \\
\end{array}
$$

### Types

And then we have the definition of types:

$$
\begin{array}{llrll}
  \textrm{mono} & \tau &=     & \alpha         & \ \textrm{TVar} \\
                &      &\vert & \gamma         & \ \textrm{TCon} \\
                &      &\vert & \tau_1\ \tau_2 & \ \textrm{TApp} \\
  \\
  \textrm{poly} & \sigma &= & \forall\ \alpha_1\dots\alpha_n.\ \tau & \ \textrm{Scheme}\\
\end{array}
$$

We have the poly types that represent [parametric polymorphism](https://en.wikipedia.org/wiki/Parametric_polymorphism)(known as generic programming in many languages). That are represented like:

$$
id : \forall \alpha.\ \alpha \rightarrow \alpha
$$

The $\forall$ represents the type parameters, and after the $.$, is the proper type. This structure can be represented in kotlin with the following classes:

```kotlin
// Type.kt

// mono τ =
sealed interface Type {
  // α
  data class Constructor(val id: String) : Type

  // 'x
  data class Variable(val id: String) : Type

  // τ_1 τ_2
  data class Application(val lhs: Type, val rhs: Type) : Type
}

// poly σ = ∀ α_1 ... α_n. τ
data class Forall(val names: Set<String>, val type: Type)

```

> We can after writing the classes, implement the [toString] function to make easier to debug, like [Type.kt](https://github.com/gabrielleeg1/ekko/blob/main/src/main/kotlin/typing/Type.kt) and [Forall.kt](https://github.com/gabrielleeg1/ekko/blob/main/src/main/kotlin/typing/Forall.kt).

### Unification and free variables

Free variables, are variables that aren't bound in any type environment, these variables can unified, for example:

$$
\begin{array}{llrll}
  \text{free}(\alpha)                     &=\ \{ \alpha \} \\
  \text{free}(\tau_1\ \tau_2)             &=\ \text{free}(\tau_1)\ +\ \text{free}(\tau_2) \\
  \text{free}(\forall\ \alpha_1\dots\alpha_n.\ \tau) &=\ \text{free}(\tau)\ -\ \text{free}(\alpha_1\dots\alpha_n)\\
  \text{free}(\Gamma \vdash e\ :\ \sigma) &=\ \text{free}(\sigma)\ -\ \text{free}(\Gamma)\\
  \text{free}(\Gamma)                     &=\ \bigcup\limits_{x\ :\ \sigma\ \in\ \Gamma}\text{free}(\sigma)\\
\end{array}
$$

...Can be implemented in kotlin as:

```kotlin
// FTV.kt
fun Environment.ftv(): Set<String> = values.flatMap { it.ftv() }.toSet()

fun Type.ftv(): Set<String> = when (this) {
  is Type.Construction -> emptySet()
  is Type.Variable -> setOf(id)
  is Type.Application -> lhs.ftv() + rhs.ftv()
}
```

The free variables are essential for unification. The unification is a computational equation, it is the act of unifying two elements one, by replacing free variables with the non free side - like in a first grade equation, and it is the core of type inference. The unification can be represented in type theory using the symbol $\sqsubseteq$.

$$
\begin{array}{llrll}
  \text{mgu}(\Gamma,\ \mathbb{N} \rightarrow \mathbb{N} \rightarrow \mathbb{N},\ \mathbb{N} \rightarrow \alpha \rightarrow \mathbb{N}) = \{\alpha \mapsto \mathbb{N}\}
\end{array}
$$

...This unifier, called here as most-general unifier, because the unify function can hold more utility functions, and it can be implemented in `Kotlin` in:

```kotlin
// Unify.kt
fun mgu(lhs: Type, rhs: Type): Substitution {
  return when {
    lhs == rhs -> emptySubst()
    lhs is Type.Variable -> lhs bind rhs
    rhs is Type.Variable -> rhs bind lhs
    lhs is Type.Application && rhs is Type.Application -> {
      val s1 = mgu(lhs.lhs, rhs.lhs)
      val s2 = mgu(lhs.rhs apply s1, rhs.rhs apply s1)

      s1 compose s2
    }

    else -> throw InferException("can not unify $lhs and $rhs")
  }
}

infix fun Type.Variable.bind(other: Type): Substitution = when {
  this == other -> emptySubstitution()
  id in other.ftv() -> throw InferException("infinite type $id in $other")
  else -> substitutionOf(id to other)
}
```

Here, we see the `substitution`, that is a pure method to implement unification, like `mutable references`(that is impure, and it isn't going to be used), and it is simply a `hash map`(known also as `tables` or `objects` in javascript), in Ekko's project, we create a type alias for the `substitution`.

The missing two functions, are `compose`, and `apply`:

- compose: applies a substitution with another
- apply: deep-transforms the non-free type variables using a substitution

And can be implemented in kotlin as:

```kotlin
infix fun Forall.apply(subst: Substitution): Forall {
  return Forall(names, type.apply(subst))
}

infix fun Type.apply(subst: Substitution): Typ {
  return when (this) {
    is Type.Application -> copy(lhs = lhs apply subst, rhs = rhs apply subst)
    is Type.Constructor -> this
    is Type.Variable -> subst[id] ?: this
  }
}

infix fun Substitution.compose(other: Substitution): Substitution {
 return plus(other).mapValues { it.value apply this }
}
```

## Generalization and Instantiation

Generalization and instantiation are the core feature of the type inference, that will rule all of the other things.

- generalization: gets the $\alpha_1\dots\alpha_n$ free variables of a $\tau$ and transforms into a scheme of $\forall\ \alpha_1\dots\alpha_n.\ \tau$
- instantiation: transforms into a $\tau$ a scheme $\sigma$ peeking the existing $\alpha_1\dots\alpha_n$ mapping into new fresh type variables ready to be unified, and thereafter, erased.

## Infer

Infer is the core process of a type-system, and with Hindley-Milner/W Algorithm it can be very simple. It consists in basically instantiation type schemes(get fresh types), and unifying.

We can start creating a `Typer` class, and adding a few items there.

```kotlin
class Typer {
  private var state: Int = 0
}
```

The state consists in the core of fresh variables, this will let we know in what stage of type inference, the instantiation is. Now we need to create an utility function to get fresh variables throughout the `state`. For that we will use letters for better readability when debugging:

```kotlin
val letters: Sequence<String> = sequence {
  var prefix = ""
  var i = 0
  while (true) {
    i++
    for (c in 'a'..'z') {
      yield("$prefix$c")
    }
    if (i > Char.MAX_VALUE.code) i = 0
    prefix += "${i.toChar()}"
  }
}
```

...The letter sequence is a lazy list that enumerates from `a, b, c ...`, and when this finish, it will be combining the letters: `aa, ba, ca, ...`.

```kotlin
fun fresh(): Type = Type.Variable(letters.elementAt(++state))
```

Now, with the `fresh` function, we can start the type inference process.

```kotlin
fun tiLiteral(literal: Literal): Type {
  return when (literal) {
    is Literal.Int -> Type.Int
    is Literal.Float -> Type.Float
    is Literal.String -> Type.String
    is Literal.Unit -> Type.Unit
  }
}
```

We can start with `tiLiteral`, that will type the constants, and for starting the `tiExpression` function, we need to write the environment class:

```kotlin
// Env.kt
typealias Environment = Map<String, Forall>

fun emptyEnvironment(): Env = emptyMap()

fun environmentOf(vararg pairs: Pair<String, Forall>): Env = mapOf(pairs = pairs)

fun Environment.ftv(): Set<String> = values.flatMap { it.ftv() }.toSet()

fun Environment.extendEnv(vararg pairs: Pair<String, Forall>): Environment {
  return this + environmentOf(pairs = pairs)
}

fun Environment.apply(subst: Substitution): Env {
  return mapValues { it.value.apply(subst) }
}
```

We use a type alias, to be easier to iterate without re-implementing all of them.

```kotlin
fun tiExpression(expression: Exp, environment: Env = emptyEnv()): Pair<Subst, Typ> {
  return when (expression) {
    is Expression.Group -> tiExpression(expression.value)

    is Expression.Lit -> emptySubstitution() to tiLiteral(expression.lit)

    is Expression.Variable -> {
      val scheme = environment[expression.id.name]
        ?: throw InferException("unbound variable: ${expression.id}")

      emptySubstitution() to instantiate(scheme)
    }

    is Expression.Application -> { /* ... */ }

    is Expression.Abstraction -> { /* ... */ }

    is Expression.Let -> { /* ... */ }
  }
}
```

The base implementation is quite simple, because it is just using the utility functions previously created. For the `EApp`, and `ELet`, we will need to unify the values to get the inferred type:

```kotlin
val tv = fresh() // this is the return type
val (s1, t1) = tiExpression(exp.lhs, env)
val (s2, t2) = tiExpression(exp.rhs, env apply s1)

val s3 = mgu(t1, t2 arrow tv)

(s3 compose s2 compose s1) to (tv apply s3)
```

> Note that we can cast the `t1` to `Type.Application`, but it would be so much hard because the `->` type isn't a variant of `Typ`, so we would need to deconstruct `t1`, and it will miss the validation part, that is essential for a good compiler. The `mgu` takes care of the _logical equation_ and the _validation_.

For `EApp`, we have to isolate the $tv$ in the returned $t1$, and composes all of substitutions to make sure all of free variables are properly unified. The logic can be seen as:

$$
\begin{array}{llrll}
  \text{mgu}(\Gamma,\ \tau_1 \rightarrow \tau_2,\ \tau_1 \rightarrow \alpha) = \{\alpha \mapsto \tau_2\}
\end{array}
$$

If $\tau_1 = \text{t2}$, and $\alpha = \text{tv}$, we get the $\tau_2 = \alpha$ after applying the substitutions.

Now for the `Expression.Let` expression, we will need to write a type inference function for the patterns/parameters:

```kotlin
fun tiPattern(pattern: Pattern, environment: Environment): Pair<Type, Environment> {
  return when (pattern) {
    is Pattern.Variable -> {
      val type = fresh()

      type to environment.extend(pattern.id.name to Forall(emptySet(), type))
    }
  }
}
```

This function is quite different, because it returns a pair of `(Type, Environment)`, the first one is going to be the type of the necessary to match the parameter or a subject in case of a match expression, and the second element is going to be the environment when successfully match the requisites.

With the `tiPattern` function we can start the `tiAlternative`, that will be essential for writing the `ELet` expression inference, as the alternatives are the base construct of a _let expression_.

```kotlin
fun tiAlternative(alternative: Alternative, env: Environment): Pair<Substitution, Type> {
  val parameters = mutableListOf<Type>()
  val newEnv = env.toMutableMap() // This can be read as the function environment

  // We can use a fold function to maintain this pure, but it would decrease the function's readability
  // This basically will infer the patterns match in the parameters using the `tiPattern`
  for (pattern in alt.patterns) {
    val (type, currentEnv) = tiPattern(pattern, newEnv)

    parameters += typ
    newEnv += currentEnv
  }

  // Now we have to match the "function" body with the "function's environment"
  val (subst, type) = tiExpression(alt.exp, newEnv)

  // Folding the [parameters] list will create the required type of a binding.
  return subst to parameters.fold(typ) { acc, next ->
    next arrow acc
  }
}
```

Now, with both of the functions wrote, we can type the inference for the `ELet` expression:

```kotlin
var newSubst = emptySubst()
var newEnv = env.toMap()

// Again, we can use a pure function like `fold`, but would be harder to read as we are using Kotlin
for (alt in exp.bindings.values) {
  val (subst, type) = tiAlternative(alt, newEnv)

  newSubst = newSubst compose subst

  // The logic is quite the same, but we need to generalize, to create type schemes, because they are going to be read in `EVar` expression.
  newEnv = newEnv.extend(alt.id.name to generalize(typ, newEnv))
}

val (subst, type) = tiExpression(exp.value, newEnv)

// Finally we can compose the substitutions and return the type.
(subst compose newSubst) to type
```

With those concepts of writing the _let expression_ and the _application expression_, we can write the _lambda/abstraction expression_, that is our `EAbs`:

```kotlin
val (tv, newEnv) = tiPattern(exp.param, env)
val (subst, typ) = tiExpression(exp.value, newEnv)

subst to ((tv arrow typ) apply subst)
```

This is quite similar to the `tiAlternative`, because is the same logic, but with a single parameter.
