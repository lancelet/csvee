package com.github.lancelet.csvee;

public final class QuoteHandling {
    public QuoteHandling(char quote, QuoteMode mode) {
        this.quote = quote;
        this.mode  = mode;
    }

    public enum QuoteMode {
        IGNORE,  // ignore quotes completely
        NORMAL,  // allow text outside quotes with a warning
        STRICT   // error for text outside quotes
    }

    public final char quote;
    public final QuoteMode mode;
}
