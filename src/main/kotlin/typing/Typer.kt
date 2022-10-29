package ekko.typing

import ekko.parsing.tree.Alternative
import ekko.parsing.tree.Exp
import ekko.parsing.tree.Lit
import ekko.parsing.tree.Pat

class Typer {
  private var state: Int = 0

  fun runInfer(exp: Exp, env: Env = emptyEnv()): Typ {
    return tiExp(exp, env).second
  }

  fun tiExp(exp: Exp, env: Env = emptyEnv()): Pair<Subst, Typ> {
    return when (exp) {
      is Exp.Group -> tiExp(exp.value, env)
      is Exp.Lit -> emptySubst() to tiLit(exp.lit)

      is Exp.Var -> {
        val scheme = env[exp.id.name] ?: throw InferException("unbound variable: ${exp.id}")

        emptySubst() to inst(scheme)
      }

      is Exp.App -> {
        val tv = fresh()
        val (s1, t1) = tiExp(exp.lhs, env)
        val (s2, t2) = tiExp(exp.rhs, env.apply(s1))

        val s3 = mgu(t1, t2 arrow tv)

        (s3 compose s2 compose s1) to (tv apply s3)
      }

      is Exp.Abs -> {
        val (tv, newEnv) = tiPat(exp.param, env)
        val (subst, typ) = tiExp(exp.value, newEnv)

        subst to ((tv arrow typ) apply subst)
      }

      is Exp.Let -> {
        var newSubst = emptySubst()
        var newEnv = env.toMap()

        for (alt in exp.bindings.values) {
          val (subst, typ) = tiAlternative(alt, newEnv)

          newSubst = newSubst compose subst
          newEnv = newEnv.extendEnv(alt.id.name to generalize(typ, newEnv))
        }

        val (subst, typ) = tiExp(exp.value, newEnv)

        (subst compose newSubst) to typ
      }
    }
  }

  fun tiAlternative(alternative: Alternative, env: Env): Pair<Subst, Typ> {
    val parameters = mutableListOf<Typ>()
    val newEnv = env.toMutableMap()

    for (pat in alternative.patterns) {
      val (typ, currentEnv) = tiPat(pat, newEnv)

      parameters += typ
      newEnv += currentEnv
    }

    val (subst, typ) = tiExp(alternative.exp, newEnv)

    return subst to parameters.fold(typ) { acc, next ->
      next arrow acc
    }
  }

  fun tiPat(pat: Pat, env: Env): Pair<Typ, Env> {
    return when (pat) {
      is Pat.Var -> {
        val typ = fresh()

        typ to env.extendEnv(pat.id.name to Forall(emptySet(), typ))
      }
    }
  }

  fun tiLit(lit: Lit): Typ {
    return when (lit) {
      is Lit.Int -> Typ.Int
      is Lit.Float -> Typ.Float
      is Lit.String -> Typ.String
      is Lit.Unit -> Typ.Unit
    }
  }

  private fun generalize(typ: Typ, env: Env): Forall {
    val names = env.ftv()

    return Forall(typ.ftv().filter { it !in names }.toSet(), typ)
  }

  private fun inst(scheme: Forall): Typ {
    val subst = scheme.names.associateWith { fresh() }

    return scheme.typ apply subst
  }

  private fun mgu(lhs: Typ, rhs: Typ): Subst {
    return when {
      lhs == rhs -> emptySubst()
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

  private infix fun VarTyp.bind(other: Typ): Subst = when {
    this == other -> emptySubst()
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
