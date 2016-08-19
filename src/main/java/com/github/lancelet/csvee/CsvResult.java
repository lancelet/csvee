package com.github.lancelet.csvee;

/**
 * Disjunction type representing either success or failure of an operation.
 */
public final class CsvResult<T> {

    public static <T> CsvResult<T> ok(T value) {
        assert(value != null);
        return new CsvResult<T>(value, null);
    }

    public static <T> CsvResult<T> fail(CsvError error) {
        assert(error != null);
        return new CsvResult<T>(null, error);
    }

    private CsvResult(T value, CsvError error) {
        this.value = value;
        this.error = error;
    }

    public boolean isSuccess() { return value != null; }

    public boolean isFailure() { return error != null; }

    public T getValue() throws NoResultException {
        if (value == null)
            throw NoResultException.instance;
        return value;
    }

    public CsvError getError() throws NoResultException {
        if (error == null)
            throw NoResultException.instance;
        return error;
    }

    private final T value;
    private final CsvError error;
}
