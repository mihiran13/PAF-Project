package com.smartcampus.dto.request;

import com.smartcampus.enums.ResourceType;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateResourceRequest {
    private String name;
    private ResourceType type;
    private String description;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private String location;
    private String imageUrl;
}
