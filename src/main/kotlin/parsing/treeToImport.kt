package ekko.parsing

import ekko.parsing.EkkoParser.UseContext
import ekko.parsing.tree.Import
import java.io.File

context(File)
fun UseContext.treeToImport(): Import {
  return Import(name.treeToIdent(), getLocationIn())
}
