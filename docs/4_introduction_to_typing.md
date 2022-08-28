# Introduction to Typing

Typing is an important feature of a programming language, like in `Rust`, and make possible to write safer code.
Our `ekko` language is going to be a `statically typed language`, this means that we can't have dynamic values like in
`JavaScript`, `Python`, `PHP`.

## Theory

Types are kinds of `sets` or `constraints` of possible values, like
in [set theory](https://en.wikipedia.org/wiki/Set_theory). We can have inference rules like:

$$ {x:\sigma \in \Gamma} \over {\Gamma \vdash x:\sigma} $$

That means, if we know that $x$ have a type $sigma$ in context $\Gamma$, so $x$ have a type $sigma$.

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
                &      &\vert & 'x             & \ \textrm{TCon} \\
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
