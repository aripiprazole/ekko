package ekko.gradle

import java.io.FilterReader
import java.io.Reader
import java.io.StringReader

class AntlrPackagingTask(reader: Reader) : FilterReader(
  StringReader("package $PACKAGE_HEADER;\n\n" + reader.readText()),
)
