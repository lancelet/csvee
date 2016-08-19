package com.github.lancelet.csvee;

/**
 * Represents a programmer error, when a result is requested from a
 * `Result<T>` that is a Failure.
 */
public class NoResultException extends RuntimeException {
    private NoResultException() {}
    public static final NoResultException instance = new NoResultException();
}
