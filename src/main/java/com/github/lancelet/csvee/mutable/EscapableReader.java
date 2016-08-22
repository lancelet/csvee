package com.github.lancelet.csvee.mutable;

import java.io.IOException;
import java.io.Reader;

/**
 * Escape-aware Reader.
 *
 * This class should be used as a streaming source of characters. It wraps a
 * `Reader` with the capability to read escape sequences that are relevant to
 * CSV, and to push-back (un-read) characters.
 */
public final class EscapableReader {

    public EscapableReader(
        Reader reader, char escape, char quote, char separator
    ) {
        this.reader    = reader;
        this.escape    = (int) escape;
        this.quote     = (int) quote;
        this.separator = (int) separator;
    }

    /**
     * Returns the next character from the Reader, or -1 if no character is
     * available.
     *
     * To determine the status, call the `getStatus()` method. A return value
     * of -1 does NOT always mean EOF; it may correspond to EOF, an invalid
     * escape, or an IOException.
     */
    public final int next() {
        if (status != STATUS_OK) {
            return -1;
        } else if (havePushback) {
            havePushback = false;
            return pushback;
        } else {
            int c;
            try {
                c = reader.read();
                if (c == -1) {
                    status = STATUS_EOF;
                    return -1;
                } else if (c == escape) {
                    c = reader.read();
                    if (c == -1) {
                        status = STATUS_EOF;
                        return -1;
                    } else if (c != escape && c != quote && c != separator) {
                        status = STATUS_INVALID_ESCAPE;
                        return -1;
                    }
                }
            } catch (IOException ex) {
                ioException = ex;
                status = STATUS_IOEXCEPTION;
                return -1;
            }
            return c;
        }
    }

    /**
     * Pushes a character back to the Reader. (The reader can only accept a
     * single character push-back).
     */
    public final void push(char c) {
        havePushback = true;
        pushback = c;
    }

    public final IOException getException() {
        assert(status == STATUS_IOEXCEPTION);
        return ioException;
    }

    public final byte getStatus() { return status; }

    public static byte STATUS_OK             = 0;
    public static byte STATUS_EOF            = 1;
    public static byte STATUS_INVALID_ESCAPE = 2;
    public static byte STATUS_IOEXCEPTION    = 3;

    //----------------------------------------------------------------- Private

    private final Reader reader;
    private final int escape;
    private final int quote;
    private final int separator;

    private byte status = STATUS_OK;
    private IOException ioException = null;

    private boolean havePushback = false;
    private char pushback = 'X';
}
