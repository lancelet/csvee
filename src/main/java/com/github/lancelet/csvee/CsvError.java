package com.github.lancelet.csvee;

public final class CsvError {
    public CsvError(Type type, int column) {
        this.type   = type;
        this.column = column;
    }

    public final Type type;
    public final int column;

    public static enum Type {
        ClosingQuoteMissing,
        FreeQuoteInUnquotedField,
        FieldNotFollowedBySeparator,
        LineTerminatedBySeparator
    }
}
