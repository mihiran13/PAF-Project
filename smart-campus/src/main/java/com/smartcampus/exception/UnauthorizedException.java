package com.smartcampus.exception;

/**
 * Thrown when authentication fails or is missing entirely.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
