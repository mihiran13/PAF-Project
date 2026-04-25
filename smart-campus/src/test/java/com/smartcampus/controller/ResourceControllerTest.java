package com.smartcampus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.dto.request.CreateResourceRequest;
import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.ResourceType;
import com.smartcampus.security.JwtAuthenticationFilter;
import com.smartcampus.security.JwtTokenProvider;
import com.smartcampus.service.ResourceService;
import com.smartcampus.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResourceController.class)
public class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResourceService resourceService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter; // Only needed if Custom filter causes breaks, mocking
                                                             // usually bypasses it securely via webMvc setup

    @Test
    void testCreateResource_WithoutAuth_Returns401() throws Exception {
        CreateResourceRequest req = new CreateResourceRequest();
        req.setName("Test");

        mockMvc.perform(post("/api/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateResource_WithUserRole_Returns403() throws Exception {
        CreateResourceRequest req = new CreateResourceRequest();
        req.setName("Test");

        mockMvc.perform(post("/api/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateResource_WithAdminRoleAndValidBody_Returns201() throws Exception {
        CreateResourceRequest req = new CreateResourceRequest();
        req.setName("Computer Lab 1");
        req.setType(ResourceType.LECTURE_HALL);
        req.setCapacity(30);
        req.setStatus(ResourceStatus.ACTIVE);

        when(resourceService.createResource(any(), any())).thenReturn(null);

        mockMvc.perform(post("/api/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateResource_WithAdminRoleAndMissingName_Returns400() throws Exception {
        CreateResourceRequest req = new CreateResourceRequest();
        req.setType(ResourceType.LECTURE_HALL);

        mockMvc.perform(post("/api/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAllResources_WithAuth_Returns200() throws Exception {
        mockMvc.perform(get("/api/resources")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetResourceById_WithValidId_Returns200() throws Exception {
        when(resourceService.getResourceById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/resources/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateResource_WithAdmin_Returns200() throws Exception {
        mockMvc.perform(put("/api/resources/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testDeleteResource_WithUserRole_Returns403() throws Exception {
        mockMvc.perform(delete("/api/resources/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteResource_WithAdmin_Returns200() throws Exception {
        mockMvc.perform(delete("/api/resources/1"))
                .andExpect(status().isOk());
    }
}
