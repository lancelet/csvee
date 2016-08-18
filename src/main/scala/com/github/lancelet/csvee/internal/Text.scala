package com.github.lancelet.csvee.internal

import scala.annotation.tailrec

/**
 * Fast functional-style scanning of strings.
 *
 * Scanning on a regular `String` requires operations such as splitting to
 * create new `Strings`, which in turn may copy the underlying character array.
 * (This is a JDK implementation detail, but currently the underlying character
 * array is copied to avoid memory leaks.) For our purposes, it's really more
 * efficient to use the same underlying `String` and maintain indices which
 * describe a slice of interest. This is the purpose of the `Text` trait. It
 * should be implemented as an efficient way to scan a `String`, given that the
 * `String` is already in memory, and we'll probably require most of it.
 *
 * The `Text` trait also includes the `splitBeforeIsolated` method, which is
 * relatively unique and allows splitting operations to avoid escape characters
 * effectively.
 */
trait Text {
  /** Checks if the `Text` is empty. */
  def isEmpty: Boolean

  /** Returns the `Text` as a `String`. */
  def materialize: String

  /** Checks if the `Text` object starts with a given character. */
  def startsWith(c: Char): Boolean

  /**
   * Splits the `Text` just before a given character.
   *
   * If the character `c` never occurs in the `Text`, then this method returns
   * a tuple of this `Text` and an empty `Text` object.
   */
  def splitBefore(c: Char): (Text, Text)

  /**
   * Splits the `Text` just before an unescaped occurrence of a given character.
   *
   * In order to split, the character `c` must occur in a location where it is
   * not preceded by the `escape` character. If the character `c` never occurs
   * in an unescaped position in the `Text`, then this method returns a tuple
   * of this `Text` and an empty `Text` object.
   */
  def splitBeforeUnescaped(c: Char, escape: Char): (Text, Text)

  /**
   * Optionally returns the `Text` without the starting character.
   *
   * If the `Text` is empty, then `None` is returned.
   */
  def tail: Option[Text]

  /** Checks if the `Text` contains the given character. */
  def contains(c: Char): Boolean
}

object Text {
  /** Creates an instance of a Text object that wraps an underlying String. */
  def apply(wrapped: String): Text = SText(wrapped, 0, wrapped.length)

  /** Canonical empty Text object. */
  def empty: Text = SText("", 0, 0)
}


/**
 * Default implementation of the `Text` trait.
 *
 * @param underlying `String` to wrap / reference
 * @param beg start index of the `Text` relative to the underlying `String`
 * @param end one index past the end of the `Text` relative to the underlying
 *        `String`
 */
private final case class SText(
  underlying: String,
  beg: Int,
  end: Int
) extends Text {
  def isEmpty: Boolean = beg == end

  def materialize: String = underlying.substring(beg, end)

  def startsWith(c: Char): Boolean =
    if (isEmpty) false else (underlying(beg) == c)

  def splitBefore(c: Char): (Text, Text) = {
    val i = underlying.indexOf(c, beg)
    if (i == -1 || i >= end)
      (this, Text.empty)
    else
      (SText(underlying, beg, i), SText(underlying, i, end))
  }

  def splitBeforeUnescaped(c: Char, escape: Char): (Text, Text) = {
    @tailrec
    def findIndex(start: Int): Int = {
      assert(c != escape)

      val esci = underlying.indexOf(escape, start)
      val chri = underlying.indexOf(c,      start)

      if (chri == -1 || chri >= end)
        -1
      else if (esci == -1 || chri != (esci + 1))
        chri
      else
        findIndex(chri + 1)
    }

    @tailrec
    def findIndexCE(start: Int): Int = {
      assert (c == escape)

      val i = underlying.indexOf(c, start)
      if (i == -1 || i >= end)
        -1
      else if (i >= (end - 1) || underlying(i + 1) != c)
        i
      else
        findIndexCE(i + 2)
    }

    val j =
      if (c != escape)
        findIndex(beg)
      else
        findIndexCE(beg)

    if (j == -1)
      (this, Text.empty)
    else
      (SText(underlying, beg, j), SText(underlying, j, end))
  }

  def tail: Option[Text] =
    if (isEmpty) None else Some(SText(underlying, beg+1, end))

  def contains(c: Char): Boolean = {
    val i = underlying.indexOf(c, beg)
    if (i == -1 || i >= end) false else true
  }
}
