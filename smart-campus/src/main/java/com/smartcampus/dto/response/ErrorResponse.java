package com.smartcampus.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized Error Response for API failures.
 */
@Data
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, Object> details;

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(int status, String error, String message, String path, Map<String, Object> details) {
        this(status, error, message, path);
        this.details = details;
    }
}
