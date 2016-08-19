package com.github.lancelet.csvee;

/**
 * Disjunction type representing either success or failure of an operation.
 */
public final class Result<OK, KO> {

    @SuppressWarnings("unchecked")
    public static <T> Result<T, ?> ok(T result) {
        return new Result(result, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<?, T> ko(T failure) {
        return new Result(null, failure);
    }

    private Result(OK result, KO failure) {
        this.result  = result;
        this.failure = failure;
    }

    public boolean isSuccess() { return result != null; }

    public boolean isFailure() { return failure != null; }

    public OK getResult() {
        if (result == null)
            throw NoResultException.instance;
        else
            return result;
    }

    public KO getFailure() {
        if (failure == null)
            throw NoResultException.instance;
        else
            return failure;
    }

    private final OK result;
    private final KO failure;
}
