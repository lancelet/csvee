package org.csvee

final case class CsvEncode(config: CsvConfig = CsvConfig.default) {

  import config._

  /**
   * Encodes a single line of a CSV file.
   *
   * @param fields field to encode
   * @return encoded line (without terminating newline character)
   */
  def encodeLine(fields: TraversableOnce[String]): String = {
    val sb = new StringBuilder()
    val it = fields.toIterator
    while (it.hasNext) {
      sb.append(quoteIt(escapeIt(it.next())))
      if (it.hasNext) sb.append(separator)
    }
    sb.toString
  }

  //------------------------------------------------------------------- Private

  private def quoteIt(s: String): String =
    s"$quote$s$quote"

  private def escapeIt(s: String): String =
    s.replace(quote.toString, s"$escape$quote")

}
