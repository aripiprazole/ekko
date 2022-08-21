package ekko.parser

import ekko.parser.EkkoParser.EAbsContext
import ekko.parser.EkkoParser.EAppContext
import ekko.parser.EkkoParser.EDecimalContext
import ekko.parser.EkkoParser.EGroupContext
import ekko.parser.EkkoParser.EIntContext
import ekko.parser.EkkoParser.ELetContext
import ekko.parser.EkkoParser.EStringContext
import ekko.parser.EkkoParser.EVarContext
import ekko.parser.EkkoParser.ExpContext
import ekko.tree.EAbs
import ekko.tree.EApp
import ekko.tree.EGroup
import ekko.tree.ELet
import ekko.tree.ELit
import ekko.tree.EVar
import ekko.tree.Exp
import ekko.tree.LFloat
import ekko.tree.LInt
import ekko.tree.LString

fun ExpContext.treeToExp(): Exp {
  return when (this) {
    is ELetContext -> {
      val names = findAlt().map { it.treeToAlt() }.associateBy { it.id }
      val value = value!!.treeToExp()

      ELet(names, value)
    }

    is EAppContext -> {
      val lhs = lhs!!.treeToExp()
      val rhs = rhs!!.treeToExp()

      EApp(lhs, rhs)
    }

    is EVarContext -> {
      val ident = value!!.treeToIdent()

      EVar(ident)
    }

    is EGroupContext -> {
      val value = value!!.treeToExp()

      EGroup(value)
    }

    is EStringContext -> {
      val text = value!!.text!!.substring(1, value!!.text!!.length - 1)

      ELit(LString(text))
    }

    is EDecimalContext -> {
      val float = value!!.text!!.toFloat()

      ELit(LFloat(float))
    }

    is EIntContext -> {
      val int = value!!.text!!.toInt()

      ELit(LInt(int))
    }

    is EAbsContext -> {
      val param = param!!.treeToPat()
      val value = value!!.treeToExp()

      EAbs(param, value)
    }

    else -> throw IllegalArgumentException("Unsupported expression: ${this::class}")
  }
}
