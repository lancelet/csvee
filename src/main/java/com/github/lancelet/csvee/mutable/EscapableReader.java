package com.github.lancelet.csvee.mutable;

import java.io.IOException;
import java.io.Reader;

/**
 * Reader work-alike that knows about escape sequences.
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
     * Returns the next character from the Reader, or -1 if some non-character
     * status occurred.
     *
     * To determine the nature of the status, call the `getStatus()` method.
     */
    public int next() {
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
                haveChar = true;
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
    public void unNext(char c) {
        havePushback = true;
        pushback = c;
    }

    public IOException getException() {
        assert(status == STATUS_IOEXCEPTION);
        return ioException;
    }

    public byte getStatus() { return status; }

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

    private boolean haveChar = false;
    private boolean havePushback = false;
    private char pushback = 'X';
}
