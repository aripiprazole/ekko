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
import java.io.File

fun ExpContext.treeToExp(file: File): Exp {
  return when (this) {
    is ELetContext -> {
      val names = findAlt().map { it.treeToAlt(file) }.associateBy { it.id }
      val value = value!!.treeToExp(file)

      ELet(names, value, getLocationIn(file))
    }

    is EAppContext -> {
      val lhs = lhs!!.treeToExp(file)
      val rhs = rhs!!.treeToExp(file)

      EApp(lhs, rhs, getLocationIn(file))
    }

    is EVarContext -> {
      val ident = value!!.treeToIdent(file)

      EVar(ident, getLocationIn(file))
    }

    is EGroupContext -> {
      val value = value!!.treeToExp(file)

      EGroup(value, getLocationIn(file))
    }

    is EStringContext -> {
      val text = value!!.text!!.substring(1, value!!.text!!.length - 1)

      ELit(LString(text, getLocationIn(file)))
    }

    is EDecimalContext -> {
      val float = value!!.text!!.toFloat()

      ELit(LFloat(float, getLocationIn(file)))
    }

    is EIntContext -> {
      val int = value!!.text!!.toInt()

      ELit(LInt(int, getLocationIn(file)))
    }

    is EAbsContext -> {
      val param = param!!.treeToPat(file)
      val value = value!!.treeToExp(file)

      EAbs(param, value, getLocationIn(file))
    }

    else -> throw IllegalArgumentException("Unsupported expression: ${this::class}")
  }
}
