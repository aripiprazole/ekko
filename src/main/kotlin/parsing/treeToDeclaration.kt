package ekko.parsing

import ekko.parsing.EkkoParser.DeclContext
import ekko.parsing.tree.Declaration
import java.io.File

context(File)
fun DeclContext.treeToDeclaration(): Declaration {
  return Declaration.Alternative(alt().treeToAlternative())
}
