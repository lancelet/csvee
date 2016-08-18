package com.github.lancelet.csvee

import cats.data.Xor

import org.specs2.{ScalaCheck, Specification}

class CsvEncodeSpec extends Specification with ScalaCheck { def is = s2"""
CsvEncode
=========

  - single field (ad-hoc) $single
  - field containing separator (ad-hoc) $separator
  - field containing quote (ad-hoc) $quote
  - field containing two quotes (ad-hoc) $doubleQuote

  - multiple fields (ad-hoc) $multiFields

"""

  val d = CsvDecode()
  val e = CsvEncode()

  def single =
    e.encodeLine(Vector("Hello")) must beEqualTo("\"Hello\"")

  def separator =
    e.encodeLine(Vector("Ab,Cd")) must beEqualTo("\"Ab,Cd\"")

  def quote =
    e.encodeLine(Vector("He\"llo")) must beEqualTo("\"He\"\"llo\"")

  def doubleQuote =
    e.encodeLine(Vector("He\"\"llo")) must beEqualTo("\"He\"\"\"\"llo\"")

  def multiFields = {
    val fields = Vector("Foo\"", ",Bar", "\\Baz")
    val encoded = e.encodeLine(fields)
    (encoded must beEqualTo("\"Foo\"\"\",\",Bar\",\"\\Baz\"")) and
    (d.decodeLine(encoded) must beEqualTo(Xor.Right(fields)))
  }

}
