package org.csvee.internal

import org.scalacheck.Prop
import org.specs2.{ScalaCheck, Specification}

class TextSpec extends Specification with ScalaCheck { def is = s2"""
Text
====

  - isEmpty is correct (ad-hoc) $isEmptyAdHoc
  - isEmpty is correct $isEmptyProp

  - materialize should round-trip strings $materializeRoundTripProp

  - startsWith is correct (ad-hoc) $startsWithAdHoc
  - startsWith should be true for first char $startsWithProp

  - splitBefore empty $splitBeforeEmpty
  - splitBefore char not present (ad-hoc) $splitBeforeMissing
  - splitBefore splits general strings $splitBeforeProp
  - splitBefore is correct (ad-hoc) $splitBeforeAdHoc

  - splitBeforeUnescaped empty $splitBeforeUneEmpty
  - splitBeforeUnescaped char not present (ad-hoc) $splitBeforeUneMissing
  - splitBeforeUnescaped unescaped char not present (ad-hoc) $splitBeforeUneUneMissing
  - splitBeforeUnescaped when escape and split are the same (ad-hoc) $splitBeforeUneES
  - splitBeforeUnescaped tricky case (ad-hoc) $splitBeforeUneTricky
  - splitBeforeUnescaped ambiguous escape (ad-hoc) $splitBeforeUneAmb
  - splitBeforeUnescaped ambiguous escape 2 (ad-hoc) $splitBeforeUneAmb2
  - splitBeforeUnescaped unescapted char not present $splitBeforeUneMissingProp
  - splitBeforeUnescaped splits general strings $splitBeforeUneProp
  - splitBeforeUnescaped is correct (ad-hoc) $splitBeforeUneAdHoc

  - tail returns None for empty $tailEmpty
  - tail is correct (ad-hoc) $tailAdHoc
  - tail returns the tail of a string $tailProp

  - contains is correct (ad-hoc) $containsAdHoc
  - contains is correct $containsProp

"""

  def isEmptyAdHoc = {
    (Text("").isEmpty must beTrue) and
    (Text.empty.isEmpty must beTrue) and
    (Text("Hello").isEmpty must beFalse)
  }

  def isEmptyProp = prop ( (s: String) =>
    if (s.isEmpty)
      Text(s).isEmpty must beTrue
    else
      Text(s).isEmpty must beFalse
  )

  def materializeRoundTripProp =
    prop( (s: String) => Text(s).materialize === s )

  def startsWithAdHoc = {
    val s = Text("Hello")
    (s.startsWith('H') must beTrue) and
    (s.startsWith('W') must beFalse) and
    (Text.empty.startsWith('H') must beFalse)
  }

  def startsWithProp = prop( (s: String) =>
    if (s.isEmpty)
      Text(s).startsWith(' ') === false
    else
      Text(s).startsWith(s.head) === true
  )

  def splitBeforeEmpty =
    Text.empty.splitBefore(',') must beEqualTo((Text.empty, Text.empty))

  def splitBeforeMissing =
    Text("Hello").splitBefore(',') must beEqualTo((Text("Hello"), Text.empty))

  def splitBeforeProp = prop (
    (s1: String, s2: String) =>
    (!s1.contains(',') && !s2.contains(',')) ==>
      {
        val (lt, rt) = Text(s1 + "," + s2).splitBefore(',')
        val (l, r) = (lt.materialize, rt.materialize)
        (l === s1) and (r === ("," + s2))
      }
  )

  def splitBeforeAdHoc = {
    val (lt, rt) = Text("Hello,World").splitBefore(',')
    val (l, r) = (lt.materialize, rt.materialize)
    (l must beEqualTo("Hello")) and
    (r must beEqualTo(",World"))
  }

  def splitBeforeUneEmpty =
    Text.empty.splitBeforeUnescaped(',', '\\') must
      beEqualTo((Text.empty), Text.empty)

  def splitBeforeUneMissing = {
    val expected = (Text("Hello"), Text.empty)
    Text("Hello").splitBeforeUnescaped(',', '\\') must beEqualTo(expected)
  }

  def splitBeforeUneUneMissing = {
    val expected = (Text("ab\\,cd"), Text.empty)
    Text("ab\\,cd").splitBeforeUnescaped(',', '\\') must beEqualTo(expected)
  }

  def splitBeforeUneES = {
    val expected = ("He", "lo")
    val (lt, rt) = Text("Helo").splitBeforeUnescaped('l', 'l')
    val r = (lt.materialize, rt.materialize)
    r must beEqualTo(expected)
  }

  def splitBeforeUneTricky = {
    val expected = ("Hello", "\",\"World\"")
    val (lt, rt) = Text("Hello\",\"World\"").splitBeforeUnescaped('"', '"')
    val r = (lt.materialize, rt.materialize)
    r must beEqualTo(expected)
  }

  def splitBeforeUneAmb = {
    val expected = ("Hell", "lo")
    val (lt, rt) = Text("Helllo").splitBeforeUnescaped('l', 'l')
    val r = (lt.materialize, rt.materialize)
    r must beEqualTo(expected)
  }

  def splitBeforeUneAmb2 = {
    val expected = (Text("Hellllo"), Text.empty)
    Text("Hellllo").splitBeforeUnescaped('l', 'l') must beEqualTo(expected)
  }

  def splitBeforeUneMissingProp = prop(
    (s1: String, s2: String) =>
    (
      !s1.contains(',')  &&
      !s1.contains('\\') &&
      !s2.contains(',')  &&
      !s2.contains('\\')
    ) ==>
      {
        val (lt, rt) = Text(s1 + "\\," + s2).splitBeforeUnescaped(',', '\\')
        val (l, r) = (lt.materialize, rt.materialize)
        (l === (s1 + "\\," + s2) and (r === ""))
      }
  )

  def splitBeforeUneProp = prop (
    (s1: String, s2: String) =>
    (
      !s1.contains(',')  &&
      !s1.contains('\\') &&
      !s2.contains(',')  &&
      !s2.contains('\\')
    ) ==>
      {
        val (lt, rt) = Text(s1 + "," + s2).splitBeforeUnescaped(',', '\\')
        val (l, r) = (lt.materialize, rt.materialize)
        (l === s1) and (r === ("," + s2))
      }
  )

  def splitBeforeUneAdHoc = {
    val (lt, rt) = Text("Hello,Nice\\, World").splitBeforeUnescaped(',', '\\')
    val (l, r) = (lt.materialize, rt.materialize)
    (l must beEqualTo("Hello")) and
    (r must beEqualTo(",Nice\\, World"))
  }

  def tailEmpty =
    Text.empty.tail must beEqualTo(None)

  def tailAdHoc =
    Text("Hello").tail.get.materialize must beEqualTo("ello")

  def tailProp = prop ( (s: String) =>
    if (s.isEmpty)
      Text(s).tail must beEqualTo(None)
    else
      Text(s).tail.get.materialize must beEqualTo(s.tail)
  )

  def containsAdHoc = {
    (Text("Hello").contains('l') must beTrue) and
    (Text("Hello").contains('z') must beFalse)
  }

  def containsProp = prop (
    (s: String) =>
    (!s.isEmpty) ==>
      Prop(Text(s).contains(s.head) must beTrue)
  )
}
