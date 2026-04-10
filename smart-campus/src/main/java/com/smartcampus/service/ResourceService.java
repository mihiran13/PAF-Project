package com.smartcampus.service;

import com.smartcampus.dto.request.ChangeResourceStatusRequest;
import com.smartcampus.dto.request.CreateAvailabilityWindowRequest;
import com.smartcampus.dto.request.CreateResourceRequest;
import com.smartcampus.dto.request.UpdateResourceRequest;
import com.smartcampus.dto.response.AvailabilityWindowResponse;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.dto.response.ResourceResponse;
import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.ResourceType;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.Resource;
import com.smartcampus.model.ResourceAvailabilityWindow;
import com.smartcampus.model.User;
import com.smartcampus.repository.ResourceAvailabilityWindowRepository;
import com.smartcampus.repository.ResourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@Transactional
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceAvailabilityWindowRepository availabilityWindowRepository;

    public ResourceService(ResourceRepository resourceRepository,
            ResourceAvailabilityWindowRepository availabilityWindowRepository) {
        this.resourceRepository = resourceRepository;
        this.availabilityWindowRepository = availabilityWindowRepository;
    }

    public ResourceResponse createResource(CreateResourceRequest request, User currentUser) {
        Resource resource = Resource.builder()
                .name(request.getName())
                .type(request.getType())
                .description(request.getDescription())
                .capacity(request.getCapacity())
                .location(request.getLocation())
                .status(request.getStatus() != null ? request.getStatus() : ResourceStatus.ACTIVE)
                .imageUrl(request.getImageUrl())
                .createdBy(currentUser)
                .availabilityWindows(new ArrayList<>())
                .build();

        Resource saved = resourceRepository.save(resource);
        return ResourceResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ResourceResponse> getAllResources(String keyword, ResourceType type,
            ResourceStatus status, Integer minCapacity,
            int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Resource> resourcePage = resourceRepository.findAdvanced(
                keyword, type, status, minCapacity, pageable);

        Page<ResourceResponse> responsePage = resourcePage.map(ResourceResponse::fromEntity);
        return PagedResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));

        // Let Hibernate fetch the relationships inside the active transaction via
        // getter
        resource.getAvailabilityWindows().size();

        return ResourceResponse.fromEntity(resource);
    }

    public ResourceResponse updateResource(Long id, UpdateResourceRequest request) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));

        if (request.getName() != null)
            resource.setName(request.getName());
        if (request.getType() != null)
            resource.setType(request.getType());
        if (request.getDescription() != null)
            resource.setDescription(request.getDescription());
        if (request.getCapacity() != null)
            resource.setCapacity(request.getCapacity());
        if (request.getLocation() != null)
            resource.setLocation(request.getLocation());
        if (request.getImageUrl() != null)
            resource.setImageUrl(request.getImageUrl());

        Resource updated = resourceRepository.save(resource);
        return ResourceResponse.fromEntity(updated);
    }

    public ResourceResponse changeResourceStatus(Long id, ChangeResourceStatusRequest request) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));

        resource.setStatus(request.getStatus());
        Resource updated = resourceRepository.save(resource);
        return ResourceResponse.fromEntity(updated);
    }

    public void deleteResource(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));

        // Soft delete logic matching Phase 4.1 workflow requirements
        resource.setStatus(ResourceStatus.OUT_OF_SERVICE);
        resourceRepository.save(resource);
    }

    public AvailabilityWindowResponse addAvailabilityWindow(Long resourceId, CreateAvailabilityWindowRequest request) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", resourceId));

        if (request.getStartTime().isAfter(request.getEndTime()) ||
                request.getStartTime().equals(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        // Potential check for overlapping times here, omitting complex overlap logic
        // for brevity,
        // rely on DB constraints if strictly needed, or manually:
        boolean overlap = resource.getAvailabilityWindows().stream()
                .filter(w -> w.getDayOfWeek() == request.getDayOfWeek())
                .anyMatch(w -> (request.getStartTime().isBefore(w.getEndTime())
                        && request.getEndTime().isAfter(w.getStartTime())));

        if (overlap) {
            throw new BadRequestException(
                    "Availability window overlaps with an existing window on " + request.getDayOfWeek());
        }

        ResourceAvailabilityWindow window = ResourceAvailabilityWindow.builder()
                .resource(resource)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        resource.getAvailabilityWindows().add(window);
        resourceRepository.save(resource); // Cascades to window creation

        return AvailabilityWindowResponse.fromEntity(window);
    }

    public void removeAvailabilityWindow(Long resourceId, Long windowId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", resourceId));

        ResourceAvailabilityWindow window = availabilityWindowRepository.findById(windowId)
                .orElseThrow(() -> new ResourceNotFoundException("ResourceAvailabilityWindow", "id", windowId));

        if (!window.getResource().getId().equals(resource.getId())) {
            throw new BadRequestException("The specified availability window does not belong to this resource.");
        }

        resource.getAvailabilityWindows().remove(window);
        availabilityWindowRepository.delete(window);
    }
}
