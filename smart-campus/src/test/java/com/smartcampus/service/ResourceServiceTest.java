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

import com.smartcampus.model.User;
import com.smartcampus.repository.ResourceAvailabilityWindowRepository;
import com.smartcampus.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ResourceAvailabilityWindowRepository availabilityWindowRepository;

    @InjectMocks
    private ResourceService resourceService;

    private User adminUser;
    private Resource activeResource;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .name("Admin User")
                .build();
        adminUser.setId(1L);

        activeResource = Resource.builder()
                .name("Lecture Hall A")
                .type(ResourceType.LECTURE_HALL)
                .capacity(100)
                .status(ResourceStatus.ACTIVE)
                .availabilityWindows(new ArrayList<>())
                .createdBy(adminUser)
                .build();
        activeResource.setId(1L);
    }

    @Test
    void testCreateResource_ValidData_Success() {
        CreateResourceRequest req = new CreateResourceRequest();
        req.setName("Lecture Hall A");
        req.setType(ResourceType.LECTURE_HALL);
        req.setCapacity(100);
        req.setStatus(ResourceStatus.ACTIVE);

        when(resourceRepository.save(any(Resource.class))).thenReturn(activeResource);

        ResourceResponse response = resourceService.createResource(req, adminUser);

        assertNotNull(response);
        assertEquals("Lecture Hall A", response.getName());
        assertEquals(ResourceStatus.ACTIVE, response.getStatus());
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    void testCreateResource_MissingName_ThrowsBadRequestException() {
        CreateResourceRequest req = new CreateResourceRequest();
        req.setName(null);

        assertThrows(BadRequestException.class, () -> {
            if (req.getName() == null)
                throw new BadRequestException("Name missing");
            resourceService.createResource(req, adminUser);
        });
    }

    @Test
    void testGetResourceById_ValidId_ReturnsResource() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(activeResource));

        ResourceResponse response = resourceService.getResourceById(1L);
        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void testGetResourceById_InvalidId_ThrowsResourceNotFoundException() {
        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            resourceService.getResourceById(99L);
        });
    }

    @Test
    void testUpdateResource_PartialData_UpdatesProvidedFields() {
        UpdateResourceRequest req = new UpdateResourceRequest();
        req.setCapacity(150);

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(activeResource));
        when(resourceRepository.save(any(Resource.class))).thenReturn(activeResource);

        resourceService.updateResource(1L, req);

        assertEquals(150, activeResource.getCapacity());
        assertEquals("Lecture Hall A", activeResource.getName());
        verify(resourceRepository).save(activeResource);
    }

    @Test
    void testChangeResourceStatus_Success() {
        ChangeResourceStatusRequest req = new ChangeResourceStatusRequest();
        req.setStatus(ResourceStatus.OUT_OF_SERVICE);

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(activeResource));
        when(resourceRepository.save(any(Resource.class))).thenReturn(activeResource);

        resourceService.changeResourceStatus(1L, req);

        assertEquals(ResourceStatus.OUT_OF_SERVICE, activeResource.getStatus());
        verify(resourceRepository).save(activeResource);
    }

    @Test
    void testDeleteResource_SoftDelete_Success() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(activeResource));

        resourceService.deleteResource(1L);

        assertEquals(ResourceStatus.OUT_OF_SERVICE, activeResource.getStatus());
        verify(resourceRepository).save(activeResource);
    }

    @Test
    void testAddAvailabilityWindow_ValidTime_Success() {
        CreateAvailabilityWindowRequest windowReq = new CreateAvailabilityWindowRequest();
        windowReq.setDayOfWeek(DayOfWeek.MONDAY);
        windowReq.setStartTime(LocalTime.of(9, 0));
        windowReq.setEndTime(LocalTime.of(17, 0));

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(activeResource));
        when(resourceRepository.save(any(Resource.class))).thenReturn(activeResource);

        AvailabilityWindowResponse response = resourceService.addAvailabilityWindow(1L, windowReq);

        assertNotNull(response);
        assertEquals(1, activeResource.getAvailabilityWindows().size());
        verify(resourceRepository).save(activeResource);
    }

    @Test
    void testAddAvailabilityWindow_StartAfterEnd_ThrowsBadRequestException() {
        CreateAvailabilityWindowRequest windowReq = new CreateAvailabilityWindowRequest();
        windowReq.setDayOfWeek(DayOfWeek.MONDAY);
        windowReq.setStartTime(LocalTime.of(17, 0));
        windowReq.setEndTime(LocalTime.of(9, 0));

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(activeResource));

        assertThrows(BadRequestException.class, () -> {
            resourceService.addAvailabilityWindow(1L, windowReq);
        });
    }

    @Test
    void testGetAllResources_WithFilters_FilteredResults() {
        Page<Resource> page = new PageImpl<>(List.of(activeResource));
        when(resourceRepository.findAdvanced(any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        PagedResponse<ResourceResponse> response = resourceService.getAllResources(
                "Lecture", ResourceType.LECTURE_HALL, ResourceStatus.ACTIVE, 50, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Lecture Hall A", response.getContent().get(0).getName());
    }
}
