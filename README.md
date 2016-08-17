csvee

> CSV library in Scala

`csvee` is a small, efficient library for reading and writing
[RFC 4180](https://www.ietf.org/rfc/rfc4180.txt)-compatible CSV files in
Scala. It handles correct escaping of fields in CSV.

## Getting Started

`csvee` currently only encodes and decodes single lines from a CSV file (both
assumed to be without newlines):

```scala
import cats.Xor
import org.csvee.{CsvEncode, CsvDecode, CsvError}

object Example {
  val encoder = CsvEncode()
  val decoder = CsvDecode()

  val fields = Vector("csvee", "42", ", comma", "quote \"")
  val encoded: String = encoder.encodeLine(fields)
  val decoded: Xor[CsvError, Vector[String]] = decoder.decodeLine(encoded)
  
  // or, with a size hint
  val decodedSizeHint = decoder.decodeLine(encoded, Some(fields.length))
}
```

## Why not OpenCSV?

[OpenCSV](http://opencsv.sourceforge.net/) is the project which prompted the
original creation of `csvee`. Deficiencies were identified because OpenCSV:
  - was unable to round-trip a field containing a single backslash character
    (as of version 3.8),
  - is closely coupled to the `Reader` and `Writer` abstractions in Java,
  - is hosted on SourceForge,
  - is Java-based and throws exceptions.
