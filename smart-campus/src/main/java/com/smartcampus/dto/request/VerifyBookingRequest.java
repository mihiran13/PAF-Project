package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyBookingRequest {
    @NotBlank(message = "Verification token is required")
    private String token;
}
