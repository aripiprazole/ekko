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

$$
\begin{array}{lrll}
  e & =     & x                                   & \textrm{EVar}\\
    & \vert & e_1\ e_2                            & \textrm{EApp}\\
    & \vert & \lambda\ x\ .\ e                    & \textrm{EAbs} \\
    & \vert & \mathtt{let}\ x = e_1\ \mathtt{in}\ e_2 &\\
\end{array}
$$

The expressions, that are based in lambda-calculus, extended with a let-expression. And then we have the definition of types:

$$
\begin{array}{llrll}
  \textrm{mono} & \tau &=     & \alpha         & \ \textrm{TVar} \\
                &      &\vert & 'x             & \ \textrm{TCon} \\
                &      &\vert & \tau_1\ \tau_2 & \ \textrm{TApp} \\
  \\
  \textrm{poly} & \sigma &= & \forall\ \alpha_1\dots\alpha_n\ .\ \tau & \ \textrm{Scheme}\\
\end{array}
$$

The type system, have [parametric polymorphism](https://en.wikipedia.org/wiki/Parametric_polymorphism)(known as generic programming in many languages), so we have the type schemes to represent it.
