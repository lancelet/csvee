package com.github.lancelet.csvee;

import java.lang.reflect.Array;
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

    @SuppressWarnings("unchecked")
    public Result<String[], CsvError> decodeLine(String input, int sizeHint) {
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
                if (fieldEnd == -1) {
                    return (Result<String[], CsvError>)
                        Result.ko(new CsvError("Field was missing a closing quote."));
                }
                field = unescape(input.substring(fieldStart, fieldEnd));
                i = fieldEnd + 1;
            } else {
                // a non-quoted field
                fieldStart = i;
                fieldEnd = nextSeparator(input, fieldStart);
                field = input.substring(fieldStart, fieldEnd);
                if (field.contains(quoteCharSeq)) {
                    return (Result<String[], CsvError>)
                        Result.ko(new CsvError("Free quote occurs in unquoted field."));
                }
                i = fieldEnd;
            }

            // skip over the separator
            if (i < input.length() && input.charAt(i) != separator) {
                return (Result<String[], CsvError>)
                    Result.ko(new CsvError("Field was not followed by a separator."));
            }
            i++;

            // check for a terminal separator on the line
            if (i == input.length()) {
                return (Result<String[], CsvError>)
                    Result.ko(new CsvError("Line was terminated by a separator."));
            }

            buffer.add(field);
        }

        String[] strings = (String[])(Array.newInstance(String.class, buffer.size()));
        buffer.toArray(strings);

        return (Result<String[], CsvError>) Result.ok(strings);
    }

    private String unescape(String str) {
        return str.replace(escapedQuote, quoteCharSeq);
    }

    private int nextSeparator(String str, int start) {
        int i = str.indexOf(separator, start);
        return (i == -1) ? str.length() : i;
    }

    private int unescapedIndexOf(String str, char c, int start) {
        int i;
        if (c == escape) {
            // case where c happens to match the escape character
            i = start - 2;
            do {
                i = str.indexOf(c, i + 2); 
            } while (
                     (i != -1) &&
                     (i < (str.length()-1) && str.charAt(i+1) == c)
                    );
        } else {
            // case where c does not match the escape character
            i = start - 1;
            int esci;
            do {
                i += 1;
                i    = str.indexOf(c,      i);
                esci = str.indexOf(escape, i);
            } while (
                     (i != -1) &&
                     (esci != -1 && i == (esci+1))
                    );
        }
        return i;
    }

    private final char separator;
    private final char quote;
    private final char escape;

    private final CharSequence escapedQuote;
    private final CharSequence quoteCharSeq;
}
