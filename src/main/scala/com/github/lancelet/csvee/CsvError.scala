package com.github.lancelet.csvee

sealed trait CsvError

case object CsvErrorFreeQuoteInField        extends CsvError
case object CsvErrorExpectedSeparator       extends CsvError
case object CsvErrorSeparatorTerminatedLine extends CsvError
case object CsvErrorMissingQuote            extends CsvError
