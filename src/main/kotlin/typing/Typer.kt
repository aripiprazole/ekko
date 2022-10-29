package ekko.typing

import ekko.parsing.tree.Alternative
import ekko.parsing.tree.Expression
import ekko.parsing.tree.Literal
import ekko.parsing.tree.Pattern

class Typer {
  private var state: Int = 0

  fun runInfer(expression: Expression, environment: Environment = emptyEnvironment()): Typ {
    return tiExpression(expression, environment).second
  }

  fun tiExpression(
    expression: Expression,
    environment: Environment = emptyEnvironment(),
  ): Pair<Substitution, Typ> {
    return when (expression) {
      is Expression.Group -> tiExpression(expression.value, environment)
      is Expression.Literal -> emptySubstitution() to tiLiteral(expression.literal)

      is Expression.Variable -> {
        val scheme = environment[expression.id.name]
          ?: throw InferException("unbound variable: ${expression.id}")

        emptySubstitution() to inst(scheme)
      }

      is Expression.Application -> {
        val tv = fresh()
        val (s1, t1) = tiExpression(expression.lhs, environment)
        val (s2, t2) = tiExpression(expression.rhs, environment.apply(s1))

        val s3 = mgu(t1, t2 arrow tv)

        (s3 compose s2 compose s1) to (tv apply s3)
      }

      is Expression.Abstraction -> {
        val (tv, newEnv) = tiPattern(expression.parameter, environment)
        val (subst, typ) = tiExpression(expression.value, newEnv)

        subst to ((tv arrow typ) apply subst)
      }

      is Expression.Let -> {
        var newSubst = emptySubstitution()
        var newEnv = environment.toMap()

        for (alt in expression.bindings.values) {
          val (subst, typ) = tiAlternative(alt, newEnv)

          newSubst = newSubst compose subst
          newEnv = newEnv.extendEnv(alt.id.name to generalize(typ, newEnv))
        }

        val (subst, typ) = tiExpression(expression.value, newEnv)

        (subst compose newSubst) to typ
      }
    }
  }

  fun tiAlternative(alternative: Alternative, environment: Environment): Pair<Substitution, Typ> {
    val parameters = mutableListOf<Typ>()
    val newEnv = environment.toMutableMap()

    for (pat in alternative.patterns) {
      val (typ, currentEnv) = tiPattern(pat, newEnv)

      parameters += typ
      newEnv += currentEnv
    }

    val (subst, typ) = tiExpression(alternative.expression, newEnv)

    return subst to parameters.fold(typ) { acc, next ->
      next arrow acc
    }
  }

  fun tiPattern(pattern: Pattern, environment: Environment): Pair<Typ, Environment> {
    return when (pattern) {
      is Pattern.Variable -> {
        val typ = fresh()

        typ to environment.extendEnv(pattern.id.name to Forall(emptySet(), typ))
      }
    }
  }

  fun tiLiteral(literal: Literal): Typ {
    return when (literal) {
      is Literal.Int -> Typ.Int
      is Literal.Float -> Typ.Float
      is Literal.String -> Typ.String
      is Literal.Unit -> Typ.Unit
    }
  }

  private fun generalize(typ: Typ, environment: Environment): Forall {
    val names = environment.ftv()

    return Forall(typ.ftv().filter { it !in names }.toSet(), typ)
  }

  private fun inst(scheme: Forall): Typ {
    val subst = scheme.names.associateWith { fresh() }

    return scheme.typ apply subst
  }

  private fun mgu(lhs: Typ, rhs: Typ): Substitution {
    return when {
      lhs == rhs -> emptySubstitution()
      lhs is VarTyp -> lhs bind rhs
      rhs is VarTyp -> rhs bind lhs
      lhs is AppTyp && rhs is AppTyp -> {
        val s1 = mgu(lhs.lhs, rhs.lhs)
        val s2 = mgu(lhs.rhs apply s1, rhs.rhs apply s1)

        s1 compose s2
      }

      else -> throw InferException("can not unify $lhs and $rhs")
    }
  }

  private infix fun VarTyp.bind(other: Typ): Substitution = when {
    this == other -> emptySubstitution()
    id in other.ftv() -> throw InferException("infinite type $id in $other")
    else -> substOf(id to other)
  }

  private fun fresh(): Typ = VarTyp(letters.elementAt(++state))

  private val letters: Sequence<String> = sequence {
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
}
