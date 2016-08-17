package org.csvee

/** TODO */
final case class CsvConfig(separator: Char, quote: Char, escape: Char) {
  require(
    separator != quote,
    "separator and quote characters must be different"
  )
}

object CsvConfig {
  val default: CsvConfig = CsvConfig(
    separator = ',',
    quote     = '"',
    escape    = '"'
  )
}
