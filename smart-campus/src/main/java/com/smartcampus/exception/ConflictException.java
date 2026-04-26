package com.smartcampus.exception;

import lombok.Getter;
import java.util.Map;

/**
 * Thrown specifically when booking time slots overlap or resources are double-booked.
 */
@Getter
public class ConflictException extends RuntimeException {
    
    private Map<String, Object> details;

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Map<String, Object> details) {
        super(message);
        this.details = details;
    }
}
