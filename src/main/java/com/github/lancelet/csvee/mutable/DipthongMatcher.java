package com.github.lancelet.csvee.mutable;

/**
 * Detects dipthongs / pairs of characters.
 *
 * Receives characters in sequence and indicates when a particular pair has
 * been encountered. The matcher resets after a successful match, even if the
 * first and second characters are identical.
 */
public final class DipthongMatcher {

    public DipthongMatcher(char fst, char snd) {
        this.fst = fst;
        this.snd = snd;
    }

    /**
     * Pushes a character into the matcher, and returns whether a match has
     * occurred.
     */
    public boolean push(char c) {
        if (haveFst && c == snd) {
            haveFst = false;
            return true;
        } else {
            haveFst = (c == fst);
            return false;
        }
    }


    //----------------------------------------------------------------- Private

    private final char fst;
    private final char snd;
    private boolean haveFst;
}
