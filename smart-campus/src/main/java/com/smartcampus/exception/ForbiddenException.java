package com.smartcampus.exception;

/**
 * Thrown when an authenticated user attempts to access a resource they do not have the role or ownership rights for.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
