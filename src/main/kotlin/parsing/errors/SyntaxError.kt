package ekko.parsing.errors

import ekko.parsing.tree.Location
import ekko.parsing.tree.Position
import ekko.reporting.MError
import ekko.reporting.Report
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.Token

class SyntaxError(message: String) : MError(0x01, "Syntax Error", message)

class SyntaxErrorListener(val report: Report) : BaseErrorListener() {
  override fun syntaxError(
    recognizer: Recognizer<*, *>,
    offendingSymbol: Any?,
    line: Int,
    charPositionInLine: Int,
    msg: String,
    e: RecognitionException?,
  ) {
    val token = offendingSymbol as? Token ?: error("Expecting token, got $offendingSymbol")

    val location = Location(
      start = Position(token.startIndex, report.file),
      end = Position(token.stopIndex, report.file),
    )

    report.addMessage(location) {
      SyntaxError(msg)
    }
  }
}
