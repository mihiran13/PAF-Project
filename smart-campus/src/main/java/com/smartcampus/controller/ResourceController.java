package com.smartcampus.controller;

import com.smartcampus.dto.request.ChangeResourceStatusRequest;
import com.smartcampus.dto.request.CreateAvailabilityWindowRequest;
import com.smartcampus.dto.request.CreateResourceRequest;
import com.smartcampus.dto.request.UpdateResourceRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.AvailabilityWindowResponse;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.dto.response.ResourceResponse;
import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.ResourceType;
import com.smartcampus.enums.UserRole;
import com.smartcampus.security.CurrentUser;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.ResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ResourceResponse>> createResource(
            @Valid @RequestBody CreateResourceRequest request,
            @CurrentUser CustomUserDetails currentUser) {

        ResourceResponse response = resourceService.createResource(request, currentUser.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resource created successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<ResourceResponse>>> getAllResources(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) ResourceStatus status,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @CurrentUser CustomUserDetails currentUser) {

        // For non-admin users, if no explicit status filter is requested via query,
        // enforce ACTIVE only
        if (status == null && currentUser.getUser().getRole() != UserRole.ADMIN) {
            status = ResourceStatus.ACTIVE;
        }

        PagedResponse<ResourceResponse> response = resourceService.getAllResources(
                keyword, type, status, minCapacity, page, size);
        return ResponseEntity.ok(ApiResponse.success("Resources retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ResourceResponse>> getResourceById(@PathVariable Long id) {
        ResourceResponse response = resourceService.getResourceById(id);
        return ResponseEntity.ok(ApiResponse.success("Resource retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ResourceResponse>> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody UpdateResourceRequest request) {

        ResourceResponse response = resourceService.updateResource(id, request);
        return ResponseEntity.ok(ApiResponse.success("Resource updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ResourceResponse>> changeResourceStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeResourceStatusRequest request) {

        ResourceResponse response = resourceService.changeResourceStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Resource status changed successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // API specs defined 204
    }

    @PostMapping("/{id}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AvailabilityWindowResponse>> addAvailabilityWindow(
            @PathVariable Long id,
            @Valid @RequestBody CreateAvailabilityWindowRequest request) {

        AvailabilityWindowResponse response = resourceService.addAvailabilityWindow(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Availability window added successfully", response));
    }

    @DeleteMapping("/{id}/availability/{windowId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeAvailabilityWindow(
            @PathVariable Long id,
            @PathVariable Long windowId) {

        resourceService.removeAvailabilityWindow(id, windowId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
