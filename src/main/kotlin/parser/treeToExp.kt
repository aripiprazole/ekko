package ekko.parser

import ekko.parser.EkkoParser.EAppContext
import ekko.parser.EkkoParser.EDecimalContext
import ekko.parser.EkkoParser.EIntContext
import ekko.parser.EkkoParser.EStrContext
import ekko.parser.EkkoParser.EVarContext
import ekko.parser.EkkoParser.ExpContext
import ekko.tree.EApp
import ekko.tree.ELit
import ekko.tree.EVar
import ekko.tree.Exp
import ekko.tree.LFloat
import ekko.tree.LInt
import ekko.tree.LStr

fun ExpContext.treeToExp(): Exp {
  when (this) {
    is EAppContext -> {
      val lhs = lhs!!.treeToExp()
      val rhs = rhs!!.treeToExp()

      return EApp(lhs, rhs)
    }

    is EVarContext -> {
      val ident = value!!.treeToIdent()

      return EVar(ident)
    }

    is EStrContext -> {
      val text = value!!.text!!.substring(1, value!!.text!!.length - 1)

      return ELit(LStr(text))
    }

    is EDecimalContext -> {
      val float = value!!.text!!.toFloat()

      return ELit(LFloat(float))
    }

    is EIntContext -> {
      val int = value!!.text!!.toInt()

      return ELit(LInt(int))
    }

    else -> throw IllegalArgumentException("Unsupported expression: ${this::class}")
  }
}
