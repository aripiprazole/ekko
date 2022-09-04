package ekko.parsing.errors

import ekko.reporting.MError

class SyntaxError(message: String) : MError(0x01, "Syntax Error", message)
