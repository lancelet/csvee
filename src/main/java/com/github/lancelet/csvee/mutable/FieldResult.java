package com.github.lancelet.csvee.mutable;

import java.io.IOException;

/**
 * Mutable result of reading a field.
 */
public final class FieldResult {
    public FieldResult() {}

    public String field;           // null if field is skipped or invalid
    public boolean wasQuoted;      // true if the field was quoted
    public short warnings;         // bit vector of warnings
    public short errors;           // error enumeration
    public boolean lineBreak;      // if a linebreak occurred after the field
    public IOException exception;  // null if no exception

    public static final short WARN_CHARS_AFTER_QUOTE       = 0b0001;
    public static final short WARN_SPACE_BEFORE_QUOTE      = 0b0010;
    public static final short WARN_LINE_TRAILING_SEPARATOR = 0b0100;
    public static final short WARN_FREE_QUOTE_IN_FIELD     = 0b1000;

    public static final short ERROR_IOEXCEPTION          = 1;
    public static final short ERROR_MISSING_QUOTE        = 2;
    public static final short ERROR_ESCAPED_INVALID_CHAR = 3;
}
