package com.smartcampus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.dto.request.CreateBookingRequest;
import com.smartcampus.dto.request.RejectBookingRequest;
import com.smartcampus.security.JwtAuthenticationFilter;
import com.smartcampus.security.JwtTokenProvider;
import com.smartcampus.service.BookingService;
import com.smartcampus.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testCreateBooking_WithoutAuth_Returns401() throws Exception {
        CreateBookingRequest req = new CreateBookingRequest();
        req.setResourceId(1L);

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateBooking_WithValidData_Returns201() throws Exception {
        CreateBookingRequest req = new CreateBookingRequest();
        req.setResourceId(1L);
        req.setBookingDate(LocalDate.now().plusDays(1));
        req.setStartTime(LocalTime.of(10, 0));
        req.setEndTime(LocalTime.of(12, 0));
        req.setPurpose("Study");

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetMyBookings_WithAuth_Returns200() throws Exception {
        mockMvc.perform(get("/api/bookings/my")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAllBookings_WithUserRole_Returns403() throws Exception {
        mockMvc.perform(get("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllBookings_WithAdminRole_Returns200() throws Exception {
        mockMvc.perform(get("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testApproveBooking_WithUserRole_Returns403() throws Exception {
        mockMvc.perform(patch("/api/bookings/1/approve")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testApproveBooking_WithAdminRole_Returns200() throws Exception {
        mockMvc.perform(patch("/api/bookings/1/approve")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRejectBooking_WithAdminAndValidReason_Returns200() throws Exception {
        RejectBookingRequest req = new RejectBookingRequest();
        req.setRejectionReason("Not allowed today");

        mockMvc.perform(patch("/api/bookings/1/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
