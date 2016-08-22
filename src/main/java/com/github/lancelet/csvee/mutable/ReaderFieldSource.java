package com.github.lancelet.csvee.mutable;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

import com.github.lancelet.csvee.QuoteHandling;
import com.github.lancelet.csvee.QuoteHandling.QuoteMode;


public final class ReaderFieldSource {

    public ReaderFieldSource(
        Reader reader,
        char separator,
        char escape,
        QuoteHandling qHandling
    ) {
        this.reader    = new PushbackReader(reader, 1);
        this.separator = separator;
        this.escape    = escape;
        this.qHandling = qHandling;
        this.eof       = false;
    }

    public boolean get(FieldResult fieldResult) {
        return doRead(true, fieldResult);
    }

    public boolean skip(FieldResult fieldResult) {
        return doRead(false, fieldResult);
    }

    //----------------------------------------------------------------- Private

    private boolean doRead(boolean keepField, FieldResult fieldResult) {
        emptyResult(fieldResult);
        if (keepField) {
            sBuffer = new StringBuffer();
        }

        // check if we've reached the end of the stream
        if (!nextChar(fieldResult) || eof) {
            return false;
        }

        // read quoted or unquoted field
        boolean readFieldOK;
        if (qHandling.mode != QuoteMode.IGNORE && c == qHandling.quote) {
            readFieldOK = readQuotedField(keepField, fieldResult);
        } else {
            readFieldOK = readUnquotedField(keepField, fieldResult);
        }

        // read separator or newline
        boolean readSepOK =
            readFieldOK && readSeparatorOrNewline(keepField, fieldResult);

        if (sBuffer != null) {
            fieldResult.field = sBuffer.toString();
            sBuffer = null;
        }

        return readSepOK;
    }

    private void emptyResult(FieldResult fieldResult) {
        fieldResult.field     = null;
        fieldResult.wasQuoted = false;
        fieldResult.warnings  = 0;
        fieldResult.errors    = 0;
        fieldResult.lineBreak = false;
        fieldResult.exception = null;
    }

    private boolean isNewline(char c) {
        return c == '\n' || c == '\r';
    }

    private boolean readQuotedField(boolean keepField, FieldResult fieldResult) {
        fieldResult.wasQuoted = true;
        boolean escapeActive = false;
        boolean haveCloseQuote = false;
        do {
            if (!nextChar(fieldResult) || eof) {
                return false;
            }

            if (escapeActive) {
                escapeActive = false;
                if (c == escape || c == separator || c == qHandling.quote) {
                    if (keepField) {
                        sBuffer.append(c);
                    }
                } else {
                    fieldResult.errors = FieldResult.ERROR_ESCAPED_INVALID_CHAR;
                    return false;
                }
            } else if (c == escape) {
                escapeActive = true;
            } else if (c == qHandling.quote) {
                haveCloseQuote = true;
            } else if (keepField) {
                sBuffer.append(c);
            }
        } while (!haveCloseQuote);

        return true;
    }

    private boolean readUnquotedField(boolean keepField, FieldResult fieldResult) {
        boolean escapeActive = false;
        boolean haveField = true;
        boolean allSpaces = true;
        do {
            if (!nextChar(fieldResult)) {
                return false;
            }

            if (eof) {
                haveField = true;
            } else {
                if (escapeActive) {
                    escapeActive = false;
                    if (c == escape || c == separator || c == qHandling.quote) {
                        if (keepField) {
                            sBuffer.append(c);
                        }
                    } else {
                        fieldResult.errors = FieldResult.ERROR_ESCAPED_INVALID_CHAR;
                        return false;
                    }
                } else if (c == escape) {
                    escapeActive = true;
                } else if (isNewline(c) || c == separator) {
                    if (!unNextChar(fieldResult)) {
                        return false;
                    }
                    haveField = true;
                } else if (c == qHandling.quote) {
                    if (allSpaces) {
                        fieldResult.warnings &= FieldResult.WARN_SPACE_BEFORE_QUOTE;
                        return readQuotedField(keepField, fieldResult);
                    } else {
                        fieldResult.warnings &= FieldResult.WARN_FREE_QUOTE_IN_FIELD;
                    }
                } else if (keepField) {
                    sBuffer.append(c);
                }

                if (allSpaces && !Character.isWhitespace(c)) {
                    allSpaces = false;
                }
            }
        } while (!haveField);

        return true;
    }

    private boolean readSeparatorOrNewline(boolean keepResult, FieldResult fieldResult) {
        boolean completed = false;
        do {
            if (!nextChar(fieldResult)) {
                return false;
            }

            if (eof) {
                fieldResult.lineBreak = true;
                completed = true;
            } else {
                if (c == separator) {
                    completed = true;
                } else if (isNewline(c)) {
                    fieldResult.lineBreak = true;
                    return consumeNewlines(fieldResult);
                } else if (fieldResult.wasQuoted) {
                    fieldResult.warnings &= FieldResult.WARN_CHARS_AFTER_QUOTE;
                    if (keepResult) {
                        sBuffer.append(c);
                    }
                }
            }
        } while (!completed);

        return true;
    }

    private boolean consumeNewlines(FieldResult fieldResult) {
        boolean completed = false;
        do {
            if (!nextChar(fieldResult)) {
                return false;
            }
            if (eof) {
                completed = true;
            } else {
                if (!isNewline(c)) {
                    if (!unNextChar(fieldResult)) {
                        return false;
                    }
                    completed = true;
                }
            }
        } while (!completed);
        return true;
    }

    private boolean nextChar(FieldResult fieldResult) {
        if (!eof) {
            try {
                ci = reader.read();
            } catch (IOException ex) {
                fieldResult.errors    = FieldResult.ERROR_IOEXCEPTION;
                fieldResult.exception = ex;
                return false;
            }

            if (ci == -1) {
                eof = true;
            } else {
                c = (char) ci;
            }
        }
        return true;
    }

    private boolean unNextChar(FieldResult fieldResult) {
        try {
            reader.unread(c);
        } catch (IOException ex) {
            fieldResult.errors    = FieldResult.ERROR_IOEXCEPTION;
            fieldResult.exception = ex;
            return false;
        }
        return true;
    }

    private final PushbackReader reader;
    private final char separator;
    private final char escape;
    private final QuoteHandling qHandling;

    private StringBuffer sBuffer;

    private int ci;
    private char c;
    private boolean eof;
}
