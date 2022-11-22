package ekko.parsing

import ekko.parsing.EkkoParser.ModuleContext
import ekko.parsing.tree.ParsedModule
import java.io.File

context(File)
fun ModuleContext.treeToModule(): ParsedModule {
  val name = header.name.treeToIdent()
  val imports = use().map { it.treeToImport() }.toSet()
  val declarations = decl().map { it.treeToDeclaration() }.toSet()

  return ParsedModule(name, imports, declarations, getLocationIn())
}
