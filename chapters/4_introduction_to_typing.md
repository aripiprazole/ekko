# Introduction to Typing

Typing is an important feature of a programming language, like in `Rust`, and make possible to write safer code.
Our `ekko` language is going to be a `statically typed language`, this means that we can't have dynamic values like in
`JavaScript`, `Python`, `PHP`.

## Theory

Types are kinds of `sets` or `constraints` of possible values, like
in [set theory](https://en.wikipedia.org/wiki/Set_theory). We can have inference rules like:

$$ {x:\sigma \in \Gamma} \over {\Gamma \vdash x:\sigma} $$

That means, if we know that $x$ have a type $sigma$ in context $\Gamma$, so $x$ have a type $sigma$.
