package com.smartcampus.model;

import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.ResourceType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a Smart Campus Facility or Asset.
 */
@Entity
@Table(name = "resources", indexes = {
        @Index(name = "idx_resource_type", columnList = "type"),
        @Index(name = "idx_resource_status", columnList = "status"),
        @Index(name = "idx_resource_location", columnList = "location"),
        @Index(name = "idx_resource_type_status", columnList = "type, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ResourceType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = true)
    private Integer capacity;

    @Column(nullable = false, length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ResourceStatus status = ResourceStatus.ACTIVE;

    @Column(length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ResourceAvailabilityWindow> availabilityWindows = new ArrayList<>();
}
