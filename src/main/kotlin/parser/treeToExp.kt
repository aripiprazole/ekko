package ekko.parser

import ekko.parser.EkkoParser.EAppContext
import ekko.parser.EkkoParser.EDecimalContext
import ekko.parser.EkkoParser.EGroupContext
import ekko.parser.EkkoParser.EIntContext
import ekko.parser.EkkoParser.ELetContext
import ekko.parser.EkkoParser.EStrContext
import ekko.parser.EkkoParser.EVarContext
import ekko.parser.EkkoParser.ExpContext
import ekko.tree.EApp
import ekko.tree.EGroup
import ekko.tree.ELet
import ekko.tree.ELit
import ekko.tree.EVar
import ekko.tree.Exp
import ekko.tree.LFloat
import ekko.tree.LInt
import ekko.tree.LStr

fun ExpContext.treeToExp(): Exp {
  return when (this) {
    is ELetContext -> {
      val names = names!!.findVariable().associate { variable ->
        variable.name!!.treeToIdent() to variable.value!!.treeToExp()
      }
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

    is EStrContext -> {
      val text = value!!.text!!.substring(1, value!!.text!!.length - 1)

      ELit(LStr(text))
    }

    is EDecimalContext -> {
      val float = value!!.text!!.toFloat()

      ELit(LFloat(float))
    }

    is EIntContext -> {
      val int = value!!.text!!.toInt()

      ELit(LInt(int))
    }

    else -> throw IllegalArgumentException("Unsupported expression: ${this::class}")
  }
}
