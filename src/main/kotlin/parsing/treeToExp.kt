package ekko.parsing

import ekko.parsing.EkkoParser.EAbsContext
import ekko.parsing.EkkoParser.EAppContext
import ekko.parsing.EkkoParser.EDecimalContext
import ekko.parsing.EkkoParser.EGroupContext
import ekko.parsing.EkkoParser.EInfixContext
import ekko.parsing.EkkoParser.EIntContext
import ekko.parsing.EkkoParser.ELetContext
import ekko.parsing.EkkoParser.EStringContext
import ekko.parsing.EkkoParser.EVarContext
import ekko.parsing.EkkoParser.ExpContext
import ekko.parsing.tree.EAbs
import ekko.parsing.tree.EApp
import ekko.parsing.tree.EGroup
import ekko.parsing.tree.ELet
import ekko.parsing.tree.ELit
import ekko.parsing.tree.EVar
import ekko.parsing.tree.Exp
import ekko.parsing.tree.LFloat
import ekko.parsing.tree.LInt
import ekko.parsing.tree.LString
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
      // We use `.substring()` here, because the lexer includes the " characters at the start and at
      // the end of the string.
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

    is EInfixContext -> {
      val callee = callee!!.treeToIdent(file)
      val lhs = lhs!!.treeToExp(file)
      val rhs = rhs!!.treeToExp(file)

      EApp(
        lhs = EApp(
          lhs = EVar(id = callee, location = callee.location),
          rhs = lhs,
          location = lhs.location.endIn(callee.location),
        ),
        rhs = rhs,
        location = getLocationIn(file),
      )
    }

    else -> throw IllegalArgumentException("Unsupported expression: ${this::class}")
  }
}
