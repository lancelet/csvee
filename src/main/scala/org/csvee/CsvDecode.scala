package org.csvee

import scala.annotation.tailrec
import scala.collection.immutable.{VectorBuilder, Vector}

import org.csvee.internal.Text

import cats.data.Xor

final case class CsvDecode(config: CsvConfig = CsvConfig.default) {

  import config._

  /**
   * Decodes a single line from a CSV file.
   *
   * @param input line to decode
   * @param sizeHint optional expected length of the CSV row
   */
  def decodeLine(
    input: String,
    sizeHint: Option[Int] = None
  ): Xor[CsvError, Vector[String]] =
    for {
      fields           <- decodeFields(Text(input), sizeHint)
      val materialized = fields.map(_.materialize)
      val unquoted     = materialized.map(unescape)
    } yield unquoted

  //------------------------------------------------------------------- Private

  private type Field = Text

  private def decodeFields(
    txt: Text,
    sizeHint: Option[Int]
  ): Xor[CsvError, Vector[Field]] = {
    val builder = {
      val vb = new VectorBuilder[Field]()
      sizeHint.foreach(expectedSize => vb.sizeHint(expectedSize))
      vb
    }
    accumFields(builder, txt)
  }

  private def unescape(s: String): String =
    s.replace(s"$escape$quote", s"$quote")

  @tailrec
  private def accumFields(
    fields: VectorBuilder[Field],
    txt: Text
  ): Xor[CsvError, Vector[Field]] =
    if (txt.isEmpty) {
      Xor.Right(fields.result())
    } else {
      takeFieldAndSeparator(txt) match {
        case Xor.Left(error)         => Xor.Left(error)
        case Xor.Right((field, rem)) => accumFields(fields += field, rem)
      }
    }

  private def takeFieldAndSeparator(txt: Text): Xor[CsvError, (Field, Text)] =
    for {
      fr <- takeField(txt)
      val (field, remWithSeparator) = fr
      rem <- takeSeparatorIfNonEmpty(remWithSeparator)
    } yield (field, rem)

  private def takeField(txt: Text): Xor[CsvError, (Field, Text)] =
    if (txt.startsWith(quote))
      takeQuotedField(txt)
    else
      takeUnquotedField(txt)

  private def takeQuotedField(txt: Text): Xor[CsvError, (Field, Text)] = {
    assert(txt.startsWith(quote))
    val result: Option[(Field, Text)] = for {
      afterQuote <- txt.tail
      val (field, remt) = afterQuote.splitBeforeUnescaped(quote, escape)
      rem <- remt.tail
    } yield ((field, rem))
    result match {
      case Some(r) => Xor.Right(r)
      case None    => Xor.Left(CsvErrorMissingQuote)
    }
  }

  private def takeUnquotedField(txt: Text): Xor[CsvError, (Field, Text)] = {
    val (field, rem) = txt.splitBefore(separator)
    if (field.contains(quote))
      Xor.Left(CsvErrorFreeQuoteInField)
    else
      Xor.Right((field, rem))
  }

  private def takeSeparatorIfNonEmpty(txt: Text): Xor[CsvError, Text] =
    if (txt.isEmpty)
      Xor.Right(txt)
    else if (!txt.startsWith(separator))
      Xor.Left(CsvErrorExpectedSeparator)
    else
      txt.tail match {
        case Some(r) =>
          if (r.isEmpty)
            Xor.Left(CsvErrorSeparatorTerminatedLine)
          else
            Xor.Right(r)
        case None =>
          Xor.Left(CsvErrorSeparatorTerminatedLine)
      }

}
