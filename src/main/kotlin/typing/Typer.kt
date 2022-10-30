package ekko.typing

import ekko.parsing.tree.Alternative
import ekko.parsing.tree.Expression
import ekko.parsing.tree.Literal
import ekko.parsing.tree.Pattern
import ekko.typing.tree.Forall
import ekko.typing.tree.Type
import ekko.typing.tree.apply
import ekko.typing.tree.arrow
import ekko.typing.tree.ftv

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

        emptySubstitution() to instantiate(scheme)
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
        val (s, type) = tiExpression(expression.value, newEnv)

        s to ((tv arrow type) apply s)
      }

      is Expression.Let -> {
        var newSubstitution = emptySubstitution()
        var newEnvironment = environment.toMap()

        for (alternative in expression.bindings.values) {
          val (s, typ) = tiAlternative(alternative, newEnvironment)

          newSubstitution = newSubstitution compose s
          newEnvironment = newEnvironment.extend(
            alternative.id.name to generalize(typ, newEnvironment),
          )
        }

        val (s, type) = tiExpression(expression.value, newEnvironment)

        (s compose newSubstitution) to type
      }
    }
  }

  fun tiAlternative(alternative: Alternative, environment: Environment): Pair<Substitution, Type> {
    val parameters = mutableListOf<Type>()
    val newEnvironment = environment.toMutableMap()

    for (pattern in alternative.patterns) {
      val (type, currentEnvironment) = tiPattern(pattern, newEnvironment)

      parameters += type
      newEnvironment += currentEnvironment
    }

    val (s, type) = tiExpression(alternative.expression, newEnvironment)

    return s to parameters.fold(type) { acc, next ->
      next arrow acc
    }
  }

  fun tiPattern(pattern: Pattern, environment: Environment): Pair<Type, Environment> {
    return when (pattern) {
      is Pattern.Variable -> {
        val type = fresh()

        type to environment.extend(pattern.id.name to Forall(emptySet(), type))
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

  private fun instantiate(scheme: Forall): Type {
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
    else -> substitutionOf(id to other)
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
