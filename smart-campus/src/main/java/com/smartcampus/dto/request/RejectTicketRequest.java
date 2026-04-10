package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectTicketRequest {

    @NotBlank(message = "Rejection reason is required")
    private String rejectionReason;
}
