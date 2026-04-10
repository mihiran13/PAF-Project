package com.smartcampus.service;

import com.smartcampus.enums.NotificationType;
import com.smartcampus.enums.TicketStatus;
import com.smartcampus.exception.ForbiddenException;
import com.smartcampus.model.*;
import com.smartcampus.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User owner;
    private User admin;
    private User tech;
    private Booking booking;
    private Ticket ticket;
    private TicketComment comment;
    private Notification notification;

    @BeforeEach
    void setUp() {
        owner = User.builder().name("Owner").build();
        owner.setId(1L);

        admin = User.builder().name("Admin").build();
        admin.setId(2L);

        tech = User.builder().name("Tech").build();
        tech.setId(3L);

        booking = Booking.builder()
                .user(owner)
                .build();
        booking.setId(1L);

        ticket = Ticket.builder()
                .title("Ticket 1")
                .reporter(owner)
                .build();
        ticket.setId(1L);

        comment = TicketComment.builder()
                .ticket(ticket)
                .author(tech)
                .build();
        comment.setId(1L);

        notification = Notification.builder()
                .user(owner)
                .isRead(false)
                .build();
        notification.setId(1L);
    }

    @Test
    void testCreateBookingApprovedNotification_SavesForBookingOwner() {
        notificationService.createBookingApprovedNotification(booking);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification captured = captor.getValue();
        assertEquals(owner, captured.getUser());
        assertEquals(NotificationType.BOOKING_APPROVED, captured.getType());
    }

    @Test
    void testCreateBookingRejectedNotification_SavesWithRejectionReason() {
        booking.setRejectionReason("Bad reason");
        notificationService.createBookingRejectedNotification(booking);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        assertTrue(captor.getValue().getMessage().contains("Bad reason"));
        assertEquals(NotificationType.BOOKING_REJECTED, captor.getValue().getType());
    }

    @Test
    void testCreateBookingCancelledNotification_CancelledByAdmin_SavesNotification() {
        booking.setCancelledBy(admin);
        notificationService.createBookingCancelledNotification(booking);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testCreateBookingCancelledNotification_CancelledByOwner_DoesNotSave() {
        booking.setCancelledBy(owner);
        notificationService.createBookingCancelledNotification(booking);

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testCreateTicketAssignedNotification_SavesForTechnician() {
        ticket.setAssignedTechnician(tech);
        notificationService.createTicketAssignedNotification(ticket);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        assertEquals(tech, captor.getValue().getUser());
        assertEquals(NotificationType.TICKET_ASSIGNED, captor.getValue().getType());
    }

    @Test
    void testCreateTicketStatusChangeNotification_InProgress_SavesForReporter() {
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        notificationService.createTicketStatusChangeNotification(ticket, TicketStatus.IN_PROGRESS);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        assertEquals(owner, captor.getValue().getUser());
        assertEquals(NotificationType.TICKET_IN_PROGRESS, captor.getValue().getType());
    }

    @Test
    void testCreateNewCommentNotification_ByNonOwner_NotifiesOwner() {
        notificationService.createNewCommentNotification(comment, ticket);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        assertEquals(owner, captor.getValue().getUser());
    }

    @Test
    void testCreateNewCommentNotification_ByOwner_DoesNotNotifyOwner() {
        comment.setAuthor(owner);
        notificationService.createNewCommentNotification(comment, ticket);

        // the owner shouldn't notify themselves, but if tech is present tech might be
        // notified.
        // Assuming the explicit rule: by owner DOES NOT notify owner.
        // We verify that no notification for owner is saved.
        // If tech is not assigned, then never saved.
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testMarkAsRead_ByOwner_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenReturn(notification);

        notificationService.markAsRead(1L, owner);

        assertTrue(notification.getIsRead());
    }

    @Test
    void testMarkAsRead_ByNonOwner_ForbiddenException() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        User otherUser = User.builder().build();
        otherUser.setId(99L);

        assertThrows(ForbiddenException.class, () -> notificationService.markAsRead(1L, otherUser));
    }
}
