package ekko.typing

import ekko.tree.Alt
import ekko.tree.EAbs
import ekko.tree.EApp
import ekko.tree.EGroup
import ekko.tree.ELet
import ekko.tree.ELit
import ekko.tree.EVar
import ekko.tree.Exp
import ekko.tree.LFloat
import ekko.tree.LInt
import ekko.tree.LStr
import ekko.tree.LUnit
import ekko.tree.Lit
import ekko.tree.PVar
import ekko.tree.Pat

class Infer {
  private var state: Int = 0

  fun synthExp(exp: Exp, env: Env = emptyEnv()): Pair<Subst, Typ> {
    return when (exp) {
      is EGroup -> synthExp(exp.value)
      is ELit -> emptySubst() to synthLit(exp.lit)

      is EVar -> {
        val scheme = env[exp.id.name] ?: throw InferException("unbound variable: ${exp.id}")

        emptySubst() to inst(scheme)
      }

      is EApp -> {
        val tv = fresh()
        val (s1, t1) = synthExp(exp.lhs, env)
        val (s2, t2) = synthExp(exp.rhs, env.apply(s1))

        val s3 = mgu(t1, t2 arrow tv)

        (s3 compose s2 compose s1) to (tv apply s3)
      }

      is EAbs -> {
        val (tv, newEnv) = synthPat(exp.param, env)
        val (subst, typ) = synthExp(exp.value, newEnv)

        subst to ((tv arrow typ) apply subst)
      }

      is ELet -> {
        var newSubst = emptySubst()
        var newEnv = env.toMap()

        for (alt in exp.bindings.values) {
          val (subst, typ) = synthAlt(alt, newEnv)

          newSubst = newSubst compose subst
          newEnv = newEnv.extendEnv(alt.id.name to generalize(typ, newEnv))
        }

        val (subst, typ) = synthExp(exp.value, newEnv)

        (subst compose newSubst) to typ
      }
    }
  }

  fun synthAlt(alt: Alt, env: Env): Pair<Subst, Typ> {
    val parameters = mutableListOf<Typ>()
    val newEnv = env.toMutableMap()

    for (pat in alt.patterns) {
      val (typ, currentEnv) = synthPat(pat, newEnv)

      parameters += typ
      newEnv += currentEnv
    }

    val (subst, typ) = synthExp(alt.exp, newEnv)

    return subst to parameters.fold(typ) { acc, next ->
      next arrow acc
    }
  }

  fun synthPat(pat: Pat, env: Env): Pair<Typ, Env> {
    return when (pat) {
      is PVar -> {
        val typ = fresh()

        typ to env.extendEnv(pat.id.name to Forall(emptySet(), typ))
      }
    }
  }

  fun synthLit(lit: Lit): Typ {
    return when (lit) {
      is LInt -> Typ.Int
      is LFloat -> Typ.Float
      is LStr -> Typ.Str
      is LUnit -> Typ.Unit
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

  private infix fun TVar.bind(other: Typ): Subst = when {
    this == other -> emptySubst()
    id in other.ftv() -> throw InferException("infinite type $id in $other")
    else -> substOf(id to other)
  }

  private fun fresh(): Typ = TVar(letters.elementAt(++state))

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
