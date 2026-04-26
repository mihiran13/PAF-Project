package com.smartcampus.dto.response;

import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.ResourceType;
import com.smartcampus.model.Resource;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class ResourceResponse {
    private Long id;
    private String name;
    private ResourceType type;
    private String description;
    private Integer capacity;
    private String location;
    private ResourceStatus status;
    private String imageUrl;

    private String createdByName;
    private Long createdById;

    private List<AvailabilityWindowResponse> availabilityWindows;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ResourceResponse fromEntity(Resource resource) {
        if (resource == null)
            return null;

        List<AvailabilityWindowResponse> windows = Collections.emptyList();

        // Handle lazy evaluation safely by checking if it's already fetched or empty
        if (resource.getAvailabilityWindows() != null && !resource.getAvailabilityWindows().isEmpty()) {
            windows = resource.getAvailabilityWindows().stream()
                    .map(AvailabilityWindowResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        return ResourceResponse.builder()
                .id(resource.getId())
                .name(resource.getName())
                .type(resource.getType())
                .description(resource.getDescription())
                .capacity(resource.getCapacity())
                .location(resource.getLocation())
                .status(resource.getStatus())
                .imageUrl(resource.getImageUrl())
                // Safe extraction of nested elements
                .createdByName(resource.getCreatedBy() != null ? resource.getCreatedBy().getName() : null)
                .createdById(resource.getCreatedBy() != null ? resource.getCreatedBy().getId() : null)
                .availabilityWindows(windows)
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }
}
