package com.github.lancelet.csvee.mutable;

import java.io.IOException;

public interface FieldSource {
    /**
     * Get a field in full.
     *
     * Returns `true` if a field was present; `false` if no more fields can be
     * returned (either due to error or EOF).
     */
    public boolean get(FieldResult fieldResult);

    /**
     * Skip a field (but update errors, warnings and newline).
     *
     * Returns `true` if a field was present; `false` if no more fields can be
     * returned (either due to error or EOF).
     */
    public boolean skip(FieldResult fieldResult);
}
