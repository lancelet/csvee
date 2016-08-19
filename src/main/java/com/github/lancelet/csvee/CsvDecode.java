package com.github.lancelet.csvee;

import java.util.Vector;

public final class CsvDecode {

    public CsvDecode() {
        this(CsvConfig.defaults);
    }

    public CsvDecode(CsvConfig config) {
        this.separator    = config.separator;
        this.quote        = config.quote;
        this.escape       = config.escape;

        char[] escapedQuoteArray = {config.escape, config.quote};
        this.escapedQuote = new String(escapedQuoteArray);

        char[] quoteCharSeqArray = {config.quote};
        this.quoteCharSeq = new String(quoteCharSeqArray);
    }

    public CsvResult<String[]> decodeLine(String input) {
        return decodeLine(input, -1);
    }

    public CsvResult<String[]> decodeLine(String input, int sizeHint) {
        Vector<String> buffer;
        if (sizeHint != -1)
            buffer = new Vector<String>(sizeHint);
        else
            buffer = new Vector<String>();

        int i = 0, fieldStart, fieldEnd;
        String field;
        while (i < input.length()) {
            // get the next field
            if (input.charAt(i) == quote) {
                // a quoted field
                fieldStart = i + 1;
                fieldEnd = unescapedIndexOf(input, quote, fieldStart);
                if (fieldEnd == -1)
                    return error(CsvError.Type.ClosingQuoteMissing, fieldStart);
                field = input.substring(fieldStart, fieldEnd)
                             .replace(escapedQuote, quoteCharSeq);
                i = fieldEnd + 1;
            } else {
                // a non-quoted field
                fieldStart = i;
                fieldEnd = nextSeparator(input, fieldStart);
                field = input.substring(fieldStart, fieldEnd);
                if (field.contains(quoteCharSeq))
                    return error(CsvError.Type.FreeQuoteInUnquotedField,
                                 fieldStart);
                i = fieldEnd;
            }

            // skip over the separator
            if (i < input.length() && input.charAt(i) != separator)
                return error(CsvError.Type.FieldNotFollowedBySeparator, i);
            i++;

            // check for a terminal separator on the line
            if (i == input.length())
                return error (CsvError.Type.LineTerminatedBySeparator, i-1);

            buffer.add(field);
        }

        String[] strings = new String[buffer.size()];
        buffer.toArray(strings);

        return CsvResult.ok(strings);
    }

    private int nextSeparator(String str, int start) {
        int i = str.indexOf(separator, start);
        return (i == -1) ? str.length() : i;
    }

    private int unescapedIndexOf(String s, char c, int start) {
        int l = s.length() - 1;
        int i;
        if (c == escape) {
            // case where c happens to match the escape character
            i = start - 2;
            do
                i = s.indexOf(c, i + 2); 
            while ((i != -1) && (i < l && s.charAt(i+1) == c));
        } else {
            // case where c does not match the escape character
            i = start - 1;
            int esci;
            do {
                i += 1;
                i = s.indexOf(c, i);
                esci = s.indexOf(escape, i);
            } while ((i != -1) && (esci != -1 && i == (esci+1)));
        }
        return i;
    }

    private final char separator;
    private final char quote;
    private final char escape;

    private final CharSequence escapedQuote;
    private final CharSequence quoteCharSeq;

    private static <T> CsvResult<T> error(CsvError.Type type, int col) {
        return CsvResult.fail(new CsvError(type, col));
    }
}
