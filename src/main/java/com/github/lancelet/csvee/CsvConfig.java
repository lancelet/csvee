package com.github.lancelet.csvee;

public final class CsvConfig {
    public CsvConfig(char separator, char quote, char escape) {
        this.separator = separator;
        this.quote     = quote;
        this.escape    = escape;
    }

    public final char separator;
    public final char quote;
    public final char escape;

    public static final CsvConfig defaults = new CsvConfig(',', '"', '"');
}
