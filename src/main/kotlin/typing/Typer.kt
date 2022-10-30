package ekko.typing

import ekko.parsing.tree.Alternative
import ekko.parsing.tree.Expression
import ekko.parsing.tree.Literal
import ekko.parsing.tree.Pattern

class Typer {
  private var state: Int = 0

  fun runInfer(expression: Expression, environment: Environment = emptyEnvironment()): Type {
    return tiExpression(expression, environment).second
  }

  fun tiExpression(
    expression: Expression,
    environment: Environment = emptyEnvironment(),
  ): Pair<Substitution, Type> {
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

  fun tiAlternative(alternative: Alternative, environment: Environment): Pair<Substitution, Type> {
    val parameters = mutableListOf<Type>()
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

  fun tiPattern(pattern: Pattern, environment: Environment): Pair<Type, Environment> {
    return when (pattern) {
      is Pattern.Variable -> {
        val typ = fresh()

        typ to environment.extendEnv(pattern.id.name to Forall(emptySet(), typ))
      }
    }
  }

  fun tiLiteral(literal: Literal): Type {
    return when (literal) {
      is Literal.Int -> Type.Int
      is Literal.Float -> Type.Float
      is Literal.String -> Type.String
      is Literal.Unit -> Type.Unit
    }
  }

  private fun generalize(type: Type, environment: Environment): Forall {
    val names = environment.ftv()

    return Forall(type.ftv().filter { it !in names }.toSet(), type)
  }

  private fun inst(scheme: Forall): Type {
    val subst = scheme.names.associateWith { fresh() }

    return scheme.type apply subst
  }

  private fun mgu(lhs: Type, rhs: Type): Substitution {
    return when {
      lhs == rhs -> emptySubstitution()
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

  private infix fun Type.Variable.bind(other: Type): Substitution = when {
    this == other -> emptySubstitution()
    id in other.ftv() -> throw InferException("infinite type $id in $other")
    else -> substOf(id to other)
  }

  private fun fresh(): Type = Type.Variable(letters.elementAt(++state))

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
