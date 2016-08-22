package com.github.lancelet.csvee.mutable

import java.io.{IOException, Reader, StringReader}

import org.specs2.{ScalaCheck, Specification}

class EscapableReaderSpec extends Specification with ScalaCheck {

  def is = s2"""
  - regular seq without escapes (ad-hoc) $regularA
  - regular seq without escapes (scalacheck) $regular
  - seq with escape (ad-hoc) $escapedA
  - seq with escape (scalacheck) $escaped
  - invalid escape (ad-hoc) $escapeInvalidA
  - exception test (ad-hoc) $exceptionA
  """

  import EscapableReader.{
    STATUS_EOF, STATUS_INVALID_ESCAPE, STATUS_IOEXCEPTION
  }

  def readString(s: String): (Byte, String) = {
    val r = new EscapableReader(new StringReader(s), '\\', '"', ',')
    val sb = new StringBuffer(s.length())

    var ci: Int = 0
    do {
      ci = r.next()
      if (ci != -1) sb.append(ci.asInstanceOf[Char])
    } while (ci != -1)

    return (r.getStatus(), sb.toString())
  }

  def regularA = {
    val s = "Hello World,\"Quoted\""
    readString(s) must beEqualTo((STATUS_EOF, s))
  }

  def regular = prop((s: String) => (!s.contains("\\")) ==>
    {
      readString(s) must beEqualTo((STATUS_EOF, s))
    }
  )

  def escapedA = {
    val s = "Hello\\\\Escaped"
    readString(s) must beEqualTo((STATUS_EOF, "Hello\\Escaped"))
  }

  def escaped = prop(
    (s1: String, s2: String) =>
      (!s1.contains("\\") && !s2.contains("\\")) ==>
        {
          val s = s1 + "\\\\" + s2
          val expected = s1 + "\\" + s2
          readString(s) must beEqualTo((STATUS_EOF, expected))
        }
  )

  def escapeInvalidA = {
    val s = "Invalid escape \\q blah"
    readString(s)._1 must beEqualTo(STATUS_INVALID_ESCAPE)
  }

  final class DummyReader() extends Reader {
    def close(): Unit = ???
    def read(cbuf: Array[Char], off: Int, len: Int): Int = ???
    override def read(): Int = {
      throw new IOException("DummyReader")
    }
  }

  def exceptionA = {
    val r = new EscapableReader(new DummyReader(), '\\', '"', ',')
    val ci = r.next()
    val status = r.getStatus()
    val ioe = r.getException
    (ci must beEqualTo(-1)) and
      (status must beEqualTo(STATUS_IOEXCEPTION)) and
      (ioe.getMessage must beEqualTo("DummyReader"))
  }

}
