package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignTechnicianRequest {

    @NotNull(message = "Technician user ID is required")
    private Long technicianUserId;
}
