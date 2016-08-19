package com.github.lancelet.csvee.mutable

import org.specs2.{ScalaCheck, Specification}

class DipthongMatcherSpec extends Specification with ScalaCheck { def is = s2"""
  - different chars (ad-hoc) $different
  - same chars (ad-hoc) $same
  - missing completely (scalacheck) $missing
  """

  def different = {
    val m = new DipthongMatcher('a', 'b')
    val testSeq = Seq('a', 'a', 'b', 'a', 'c', 'b', 'a', 'b')
    val expected = Seq(false, false, true, false, false, false, false, true)
    testSeq.map(m.push) must beEqualTo(expected)
  }

  def same = {
    val m = new DipthongMatcher('a', 'a')
    val testSeq = Seq('a', 'a', 'b', 'a', 'c', 'a', 'a', 'a', 'a', 'a', 'b')
    val expected = Seq(
      false, true, false, false, false, false, true, false, true, false, false
    )
    testSeq.map(m.push) must beEqualTo(expected)
  }

  def missing = prop ( (s: String) => (!s.contains("ab")) ==>
    {
      val m = new DipthongMatcher('a', 'b')
      augmentString(s).map(m.push).forall(_ == false) must beTrue
    }
  )

}
