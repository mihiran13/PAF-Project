package com.smartcampus.dto.request;

import com.smartcampus.enums.ResourceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeResourceStatusRequest {

    @NotNull(message = "Status is required")
    private ResourceStatus status;
}
