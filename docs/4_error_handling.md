# Error Handling

Error Handling is a core feature for a programming language or compiler. We can start implementing error handling, by
implementing a message reporter. This will show the reader where errors occur and what the error is.

First, before implementing the message reporter, we need to define what kind of message we will be accepting there:

```kotlin
sealed class Message(
  val code: Int,
  val name: String,
  val text: String,
) {
  abstract val color: TextColors
  abstract val prefix: String
}

abstract class MHint(code: Int, name: String, message: String) : Message(code, name, message) {
  override val color: TextColors = TextColors.brightBlue
  override val prefix: String = "hint"
}

abstract class MWarning(code: Int, name: String, message: String) : Message(code, name, message) {
  override val color: TextColors = TextColors.brightYellow
  override val prefix: String = "warning"
}

abstract class MError(code: Int, name: String, message: String) : Message(code, name, message) {
  override val color: TextColors = TextColors.brightRed
  override val prefix: String = "error"
}
```

Here, we are using the [Mordant](https://github.com/ajalt/mordant) for the color highlighting. And thereafter we can
write a utility function to print the message throughout a string builder, that will be very useful:

```kotlin
private inline fun Terminal.append(builder: StringBuilder.() -> Unit) {
  println(buildString(builder))
}
```

Now, we can start writing stubs for the highlighter:

```kotlin
class Highlight(val message: Message, val file: File, val location: Location) {
  val line = location.start.line - 1
  val start = location.start.column
  val end = location.end.column

  val indexedLines = file.readLines().mapIndexed(Int::to)

  val lines = when {
    line == 0 && indexedLines.size == 1 -> indexedLines
    line == 0 && indexedLines.size > 2 -> indexedLines.subList(0, 1)
    line == indexedLines.size -> indexedLines.subList(line - 1, line + 1)
    else -> indexedLines
  }

  val maxLineNum = lines.maxOfOrNull { it.first }!!.toString()
  val numLength = maxLineNum.length

  fun printHighlight() {
    printMessage(message)
    printLocation(location)

    lines.forEach { (num, content) ->
      printLine(num, content)

      if (num == line) {
        printIndicator(content)
      }
    }
  }

  fun printMessage(message: Message) {
    ...
  }

  fun printLocation(location: Location) {
    ...
  }

  fun printLine(num: Int, content: String) {
    ...
  }

  fun printIndicator(content: String) {
    ...
  }
}
```

The, `printMessage` and the `printLocation` implementations will be quite simple:

```kotlin
fun printLocation(location: Location) {
  terminal.append {
    append(TextColors.gray(" --> "))
    append("${file.path}:${location.start.line}:${location.start.column}")
  }
}

fun printLine(num: Int, content: String) {
  terminal.append {
    append(TextColors.gray(" %${numLength}s | ".format(num)))
    append(content)
  }
}
```

...But the reporting logic is going to be a little complex, starting with printing line:

```kotlin
fun printLine(num: Int, content: String) {
  terminal.append {
    append(TextColors.gray(" %${numLength}s | ".format(num)))
    append(content)
  }
}
```

Thereafter, we need to write the `printIndicator`, that will get the line and start/end columns, and print a line
with `^^^`, for example:

```
error[T14]: can not unify string with int 
 --> /tmp/ekko17589456592600261654.ekko:1:10
 0 | println 1000
             ^^^^
```

And for that we will write:

```kotlin
fun printIndicator(content: String) {
  val highlight = MutableList(content.length) { " " }.apply {
    for (j in start..end) {
      set(j, "^")
    }
  }

  terminal.append {
    append(TextColors.gray(" ${" ".repeat(numLength)} | "))
    append(highlight.joinToString(""))
  }
}
```
