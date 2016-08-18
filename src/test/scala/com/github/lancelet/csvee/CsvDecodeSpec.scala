package com.github.lancelet.csvee

import cats.data.Xor

import org.scalacheck.Prop
import org.specs2.{ScalaCheck, Specification}

class CsvDecodeSpec extends Specification with ScalaCheck { def is = s2"""
CsvDecode
=========

  - unquoted field (ad-hoc) $singleUnquoted 
  - quoted field (ad-hoc) $singleQuoted
  - quoted field containing separator (ad-hoc) $singleQuotedSep
  - quoted field containing quote (ad-hoc) $singleQuotedQuote
  - quoted field containing two quotes (ad-hoc) $singleQuotedDoubleQuote
  - unquoted field with single backslash (ad-hoc) $singleUnquotedBS
  - quoted field with single backslash (ad-hoc) $singleQuotedBS

  - multiple unquoted fields (ad-hoc) $multiUnquoted
  - multiple quoted fields (ad-hoc) $multiQuoted
  - combination quoted and unquoted fields (ad-hoc) $multiQU

  - error when quoted has space before comma (ad-hoc) $errQSpaceBeforeComma
  - error when quoted has space after comma (ad-hoc) $errQSpaceAfterComma
  - error when a line is terminated by comma (ad-hoc) $errCommaTerminates
  - error when a quote is missing (ad-hoc) $errMissingQuote

  - encode -> decode round-trip $roundTrip

"""

  val d = CsvDecode()
  val e = CsvEncode()

  def singleUnquoted =
    d.decodeLine("Hello") must beEqualTo(Xor.Right(Vector("Hello")))

  def singleQuoted =
    d.decodeLine("\"Hello\"") must beEqualTo(Xor.Right(Vector("Hello")))

  def singleQuotedSep =
    d.decodeLine("\"He,llo\"") must beEqualTo(Xor.Right(Vector("He,llo")))

  def singleQuotedQuote =
    d.decodeLine("\"\"\"\"") must beEqualTo(Xor.Right(Vector("\"")))

  def singleQuotedDoubleQuote =
    d.decodeLine("\"\"\"\"\"\"") must beEqualTo(Xor.Right(Vector("\"\"")))

  def singleUnquotedBS =
    d.decodeLine("\\") must beEqualTo(Xor.Right(Vector("\\")))

  def singleQuotedBS =
    d.decodeLine("\"\\\"") must beEqualTo(Xor.Right(Vector("\\")))

  def multiUnquoted =
    d.decodeLine("Hello, World,42") must beEqualTo(
      Xor.Right(
        Vector(
          "Hello",
          " World",
          "42"
        )
      )
    )

  def multiQuoted =
    d.decodeLine("\"Hello\",\" World\",\"42\"") must beEqualTo(
      Xor.Right(
        Vector(
          "Hello",
          " World",
          "42"
        )
      )
    )

  def multiQU =
    d.decodeLine("Hello,\" Wo,r\"\"ld\",42") must beEqualTo(
      Xor.Right(
        Vector(
          "Hello",
          " Wo,r\"ld",
          "42"
        )
      )
    )

  def errQSpaceBeforeComma =
    d.decodeLine("\"Hello\" ,\"World\"") must beEqualTo(
      Xor.Left(CsvErrorExpectedSeparator)
    )

  def errQSpaceAfterComma =
    d.decodeLine("\"Hello\", \"World\"") must beEqualTo(
      Xor.Left(CsvErrorFreeQuoteInField)
    )

  def errCommaTerminates =
    d.decodeLine("Hello,") must beEqualTo(
      Xor.Left(CsvErrorSeparatorTerminatedLine)
    )

  def errMissingQuote =
    d.decodeLine("\"Hello\",\"World") must beEqualTo(
      Xor.Left(CsvErrorMissingQuote)
    )

  def roundTrip = prop { (ss: Vector[String]) =>
    val decoded = d.decodeLine(e.encodeLine(ss))
    decoded must beEqualTo(Xor.Right(ss))
  }

}
