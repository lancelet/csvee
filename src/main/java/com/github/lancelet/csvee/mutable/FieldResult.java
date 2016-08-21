package com.github.lancelet.csvee.mutable;

/**
 * Mutable result of reading a field.
 */
final class FieldResult {
    public FieldResult() {}

    public String field;       // can be null if field is skipped or invalid
    public short warnings;     // bit vector of errors
    public short errors;       // error indicator
    public boolean linebreak;  // if a linebreak occurred after the field

    public static final short WARN_SPACE_AFTER_QUOTE       = 0b0001;
    public static final short WARN_SPACE_BEFORE_QUOTE      = 0b0010;
    public static final short WARN_LINE_TRAILING_SEPARATOR = 0b0100;
    public static final short WARN_FREE_QUOTE_IN_FIELD     = 0b1000;

    public static final short ERROR_MISSING_QUOTE = 1;
}
