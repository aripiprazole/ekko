package ekko.parsing.tree

sealed interface Declaration {
  val location: Location

  data class Alternative(val alternative: ekko.parsing.tree.Alternative) : Declaration {
    override val location: Location = alternative.location
  }
}
