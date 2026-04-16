package com.smartcampus.exception;

/**
 * Thrown when a workflow tries to transition illegally (e.g. OPEN ticket straight to CLOSED bypassing IN_PROGRESS).
 */
public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String entityName, String currentState, String attemptedState) {
        super(String.format("Cannot transition %s from status %s to %s", entityName, currentState, attemptedState));
    }
}
