package com.smartcampus.service;

import com.smartcampus.dto.request.AssignTechnicianRequest;
import com.smartcampus.dto.request.CreateTicketRequest;
import com.smartcampus.dto.request.RejectTicketRequest;
import com.smartcampus.dto.request.ResolveTicketRequest;
import com.smartcampus.dto.request.UpdateTicketStatusRequest;
import com.smartcampus.dto.response.TicketResponse;
import com.smartcampus.enums.TicketCategory;
import com.smartcampus.enums.TicketPriority;
import com.smartcampus.enums.TicketStatus;
import com.smartcampus.enums.UserRole;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ForbiddenException;
import com.smartcampus.exception.InvalidStateTransitionException;
import com.smartcampus.model.Ticket;
import com.smartcampus.model.User;
import com.smartcampus.repository.ResourceRepository;
import com.smartcampus.repository.TicketRepository;
import com.smartcampus.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TicketService ticketService;

    private User user;
    private User tech;
    private User admin;
    private Ticket openTicket;
    private CreateTicketRequest createReq;

    @BeforeEach
    void setUp() {
        user = User.builder().role(UserRole.USER).build();
        user.setId(1L);

        tech = User.builder().role(UserRole.TECHNICIAN).build();
        tech.setId(2L);

        admin = User.builder().role(UserRole.ADMIN).build();
        admin.setId(3L);

        openTicket = Ticket.builder()
                .reporter(user)
                .title("Broken AC")
                .category(TicketCategory.IT_EQUIPMENT)
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.HIGH)
                .locationDescription("Room 1")
                .build();
        openTicket.setId(1L);

        createReq = new CreateTicketRequest();
        createReq.setTitle("Broken AC");
        createReq.setDescription("Broken");
        createReq.setPriority(TicketPriority.HIGH);
        createReq.setCategory(TicketCategory.IT_EQUIPMENT);
        createReq.setLocationDescription("Room 1");
    }

    @Test
    void testCreateTicket_Valid_OPEN() {
        when(ticketRepository.save(any())).thenReturn(openTicket);

        TicketResponse response = ticketService.createTicket(createReq, user);

        assertNotNull(response);
        assertEquals(TicketStatus.OPEN, response.getStatus());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void testCreateTicket_NoResourceNoLocation_BadRequestException() {
        createReq.setResourceId(null);
        createReq.setLocationDescription(null);

        assertThrows(BadRequestException.class, () -> {
            ticketService.createTicket(createReq, user);
        });
    }

    @Test
    void testAssignTechnician_Valid_Success() {
        AssignTechnicianRequest req = new AssignTechnicianRequest();
        req.setTechnicianUserId(2L);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(tech));
        when(ticketRepository.save(any())).thenReturn(openTicket);

        ticketService.assignTechnician(1L, req, admin);

        assertEquals(tech, openTicket.getAssignedTechnician());
        verify(notificationService).createTicketAssignedNotification(openTicket);
    }

    @Test
    void testAssignTechnician_NonTechnician_BadRequestException() {
        AssignTechnicianRequest req = new AssignTechnicianRequest();
        req.setTechnicianUserId(1L); // user

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> ticketService.assignTechnician(1L, req, admin));
    }

    @Test
    void testUpdateTicketStatus_OpenToInProgressByTechnician_Success() {
        openTicket.setAssignedTechnician(tech);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
        when(ticketRepository.save(any())).thenReturn(openTicket);

        UpdateTicketStatusRequest req = new UpdateTicketStatusRequest();
        req.setStatus(TicketStatus.IN_PROGRESS);

        ticketService.updateTicketStatus(1L, req, tech);

        assertEquals(TicketStatus.IN_PROGRESS, openTicket.getStatus());
        verify(notificationService).createTicketStatusChangeNotification(openTicket, TicketStatus.IN_PROGRESS);
    }

    @Test
    void testUpdateTicketStatus_ByUnauthorizedUser_ForbiddenException() {
        UpdateTicketStatusRequest req = new UpdateTicketStatusRequest();
        req.setStatus(TicketStatus.IN_PROGRESS);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));

        assertThrows(ForbiddenException.class, () -> ticketService.updateTicketStatus(1L, req, tech));
    }

    @Test
    void testResolveTicket_InProgressWithNotes_RESOLVED() {
        openTicket.setStatus(TicketStatus.IN_PROGRESS);
        openTicket.setAssignedTechnician(tech);

        ResolveTicketRequest req = new ResolveTicketRequest();
        req.setResolutionNotes("Fixed");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
        when(ticketRepository.save(any())).thenReturn(openTicket);

        ticketService.resolveTicket(1L, req, tech);

        assertEquals(TicketStatus.RESOLVED, openTicket.getStatus());
        assertEquals("Fixed", openTicket.getResolutionNotes());
        verify(notificationService).createTicketStatusChangeNotification(openTicket, TicketStatus.RESOLVED);
    }

    @Test
    void testResolveTicket_FromOpen_InvalidStateTransitionException() {
        // openTicket is OPEN

        ResolveTicketRequest req = new ResolveTicketRequest();
        req.setResolutionNotes("Fixed");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));

        assertThrows(InvalidStateTransitionException.class, () -> ticketService.resolveTicket(1L, req, admin));
    }

    @Test
    void testRejectTicket_OpenWithReason_REJECTED() {
        RejectTicketRequest req = new RejectTicketRequest();
        req.setRejectionReason("Invalid");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
        when(ticketRepository.save(any())).thenReturn(openTicket);

        ticketService.rejectTicket(1L, req, admin);

        assertEquals(TicketStatus.REJECTED, openTicket.getStatus());
        assertEquals("Invalid", openTicket.getRejectionReason());
        verify(notificationService).createTicketRejectedNotification(openTicket);
    }

    @Test
    void testCloseTicket_ResolvedByAdmin_CLOSED() {
        // Actually the exact method signature is assumed updateTicketStatus to CLOSED
        // but checklist said closeTicket from resolved. Wait, actual methods list says:
        // - TicketResponse updateTicketStatus(Long ticketId, UpdateTicketStatusRequest
        // request, User currentUser)
        // Let's use updateTicketStatus because TicketService actual method list doesn't
        // have closeTicket.
        // Wait, TicketService actual methods: updateTicketStatus, resolveTicket,
        // rejectTicket.
        // I will use updateTicketStatus to close it.

        openTicket.setStatus(TicketStatus.RESOLVED);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));
        when(ticketRepository.save(any())).thenReturn(openTicket);

        UpdateTicketStatusRequest req = new UpdateTicketStatusRequest();
        req.setStatus(TicketStatus.CLOSED);

        ticketService.updateTicketStatus(1L, req, admin);

        assertEquals(TicketStatus.CLOSED, openTicket.getStatus());
        verify(notificationService).createTicketStatusChangeNotification(openTicket, TicketStatus.CLOSED);
    }

    @Test
    void testCloseTicket_ByNonAdmin_ForbiddenException() {
        openTicket.setStatus(TicketStatus.RESOLVED);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket));

        UpdateTicketStatusRequest req = new UpdateTicketStatusRequest();
        req.setStatus(TicketStatus.CLOSED);

        assertThrows(ForbiddenException.class, () -> ticketService.updateTicketStatus(1L, req, user));
    }
}
