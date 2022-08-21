package ekko.typing

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

class Infer {
  private var state: Int = 0

  fun synthExp(exp: Exp, env: Env = emptyEnv()): Typ {
    return when (exp) {
      is EGroup -> synthExp(exp.value)
      is ELit -> synthLit(exp.lit)

      is EVar -> {
        val scheme = env[exp.id.name] ?: throw InferException("unbound variable: ${exp.id}")

        inst(scheme)
      }

      is EApp -> {
        val tv = fresh()
        val t1 = synthExp(exp.lhs, env)
        val t2 = synthExp(exp.rhs, env)

        val subst = mgu(t1, t2 arrow tv)

        tv apply subst
      }

      is ELet -> {
        TODO()
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
