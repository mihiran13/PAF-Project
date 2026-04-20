package com.smartcampus.exception;

/**
 * Thrown when a requested resource (e.g. User, Booking, Ticket) cannot be found in the database.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
