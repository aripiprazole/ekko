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

```kt
// Typ.kt

// mono τ =
sealed interface Typ
  // α
  data class TCon(val id: String) : Typ

  // 'x
  data class TVar(val id: String) : Typ

  // τ_1 τ_2
  data class TApp(val lhs: Typ, val rhs: Typ) : Typ

// poly σ = ∀ α_1 ... α_n. τ
data class Forall(val names: Set<String>, val typ: Typ)
```

> We can after writing the classes, implement the [toString] function to make easier to debug, like [Typ.kt](https://github.com/gabrielleeg1/ekko/blob/main/src/main/kotlin/typing/Typ.kt) and [Forall.kt](https://github.com/gabrielleeg1/ekko/blob/main/src/main/kotlin/typing/Forall.kt).

### Unification and free variables

Free variables, are variables that aren't bound in any type environment, these variables can unified, for example:

$$
\begin{array}{llrll}
  \text{free}(\alpha)                     &=\ \{ \alpha\ } \\
  \text{free}(\tau_1\ \tau_2)             &=\ \text{free}(\tau_1)\ +\ \text{free}(\tau_2) \\
  \text{free}(\forall\ \alpha_1\dots\alpha_n\ .\ \sigma) &=\ \text{free}(\sigma)\ -\ \{\alpha_1\dots\alpha_n\}\\
  \text{free}(\Gamma \vdash e\ :\ \sigma) &=\ \text{free}(\sigma)\ -\ \text{free}(\Gamma)\\
  \text{free}(\Gamma)                     &=\ \bigcup\limits_{x\ :\ \sigma\ \in\ \Gamma}\text{free}(\sigma)\\
\end{array}
$$

...Can be implemented in kotlin as:

```kt
// FTV.kt
fun Env.ftv(): Set<String> = values.flatMap { it.ftv() }.toSet()

fun Typ.ftv(): Set<String> = when (this) {
  is TCon -> emptySet()
  is TVar -> setOf(id)
  is TApp -> lhs.ftv() + rhs.ftv()
}
```

The free variables are essential for unification. The unification is a computational equation, it is the act of unifying two elements one, by replacing free variables with the non free side - like in a first grade equation, and it is the core of type inference. The unification can be represented in type theory using the symbol $\sqsubseteq$.

$$
\begin{array}{llrll}
  \text{mgu}(\Gamma,\ \mathbb{N} \rightarrow \mathbb{N} \rightarrow \mathbb{N},\ \mathbb{N} \rightarrow \alpha \rightarrow \mathbb{N}) = \{\alpha \mapsto \mathbb{N}\}
\end{array}
$$

...This unifier, called here as most-general unifier, because the unify function can hold more utility functions, and it can be implemented in `Kotlin` in:

```kt
// Unify.kt
fun mgu(lhs: Typ, rhs: Typ): Subst {
  return when {
    lhs == rhs -> emptySubst()
    lhs is TVar -> lhs bind rhs
    rhs is TVar -> rhs bind lhs
    lhs is TApp && rhs is TApp -> {
      val s1 = mgu(lhs.lhs, rhs.lhs)
      val s2 = mgu(lhs.rhs apply s1, rhs.rhs apply s1)

      s1 compose s2
    }

    else -> throw InferException("can not unify $lhs and $rhs")
  }
}

infix fun TVar.bind(other: Typ): Subst = when {
  this == other -> emptySubst()
  id in other.ftv() -> throw InferException("infinite type $id in $other")
  else -> substOf(id to other)
}
```

Here, we see the `substitution`, that is a pure method to implement unification, like `mutable references`(that is impure, and it isn't going to be used), and it is simply a `hash map`(known also as `tables` or `objects` in javascript), in Ekko's project, we create a type alias for the `substitution`.

The missing two functions, are `compose`, and `apply`:

- compose: applies a substitution with another
- apply: deep-transforms the non-free type variables using a substitution

And can be implemented in kotlin as:

```kt
infix fun Forall.apply(subst: Subst): Forall {
  return Forall(names, typ.apply(subst))
}

infix fun Typ.apply(subst: Subst): Typ {
  return when (this) {
    is TApp -> copy(lhs = lhs apply subst, rhs = rhs apply subst)
    is TCon -> this
    is TVar -> subst[id] ?: this
  }
}

infix fun Subst.compose(other: Subst): Subst {
 return plus(other).mapValues { it.value apply this }
}
```
