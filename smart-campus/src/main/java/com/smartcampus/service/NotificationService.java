package com.smartcampus.service;

import com.smartcampus.dto.response.NotificationResponse;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.enums.NotificationType;
import com.smartcampus.enums.ReferenceType;
import com.smartcampus.enums.TicketStatus;
import com.smartcampus.exception.ForbiddenException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.Booking;
import com.smartcampus.model.Notification;
import com.smartcampus.model.Ticket;
import com.smartcampus.model.TicketComment;
import com.smartcampus.model.User;
import com.smartcampus.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // --- Core Retrieval ---

    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getUserNotifications(Long userId, Boolean isRead, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationsPage;

        if (isRead != null) {
            notificationsPage = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead,
                    pageable);
        } else {
            notificationsPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        return PagedResponse.from(notificationsPage.map(NotificationResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    public NotificationResponse markAsRead(Long notificationId, User currentUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only manage your own notifications");
        }

        notification.setIsRead(true);
        Notification saved = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(saved);
    }

    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    // --- Booking Notifications ---

    public void createBookingApprovedNotification(Booking booking) {
        String msg = "Your booking for " + booking.getResource().getName() + " on " +
                booking.getBookingDate() + " (" + booking.getStartTime() + " - " +
                booking.getEndTime() + ") has been approved.";

        buildAndSaveNotification(booking.getUser(), NotificationType.BOOKING_APPROVED,
                "Booking Approved", msg, ReferenceType.BOOKING, booking.getId());
    }

    public void createBookingRejectedNotification(Booking booking) {
        String msg = "Your booking for " + booking.getResource().getName() + " on " +
                booking.getBookingDate() + " has been rejected. Reason: " + booking.getRejectionReason();

        buildAndSaveNotification(booking.getUser(), NotificationType.BOOKING_REJECTED,
                "Booking Rejected", msg, ReferenceType.BOOKING, booking.getId());
    }

    public void createBookingCancelledNotification(Booking booking) {
        // Condition: if canceller is not owner
        if (booking.getCancelledBy() != null && !booking.getCancelledBy().getId().equals(booking.getUser().getId())) {
            String msg = "Your booking for " + booking.getResource().getName() + " on " +
                    booking.getBookingDate() + " has been cancelled by an administrator.";

            buildAndSaveNotification(booking.getUser(), NotificationType.BOOKING_CANCELLED,
                    "Booking Cancelled", msg, ReferenceType.BOOKING, booking.getId());
        }
    }

    // --- Ticket Notifications ---

    public void createTicketAssignedNotification(Ticket ticket) {
        if (ticket.getAssignedTechnician() == null)
            return;

        String msg = "You have been assigned to ticket #" + ticket.getId() + ": " + ticket.getTitle();
        buildAndSaveNotification(ticket.getAssignedTechnician(), NotificationType.TICKET_ASSIGNED,
                "Ticket Assigned", msg, ReferenceType.TICKET, ticket.getId());
    }

    public void createTicketStatusChangeNotification(Ticket ticket, TicketStatus newStatus) {
        NotificationType type;
        String title;
        String msg;

        if (newStatus == TicketStatus.IN_PROGRESS) {
            type = NotificationType.TICKET_IN_PROGRESS;
            title = "Ticket In Progress";
            msg = "Your ticket #" + ticket.getId() + " (" + ticket.getTitle() + ") is now being worked on.";
        } else if (newStatus == TicketStatus.RESOLVED) {
            type = NotificationType.TICKET_RESOLVED;
            title = "Ticket Resolved";
            msg = "Your ticket #" + ticket.getId() + " (" + ticket.getTitle() + ") has been resolved.";
        } else if (newStatus == TicketStatus.CLOSED) {
            type = NotificationType.TICKET_CLOSED;
            title = "Ticket Closed";
            msg = "Your ticket #" + ticket.getId() + " (" + ticket.getTitle() + ") has been closed.";
        } else {
            return; // Irrelevant or handled elsewhere
        }

        buildAndSaveNotification(ticket.getReporter(), type, title, msg, ReferenceType.TICKET, ticket.getId());
    }

    public void createTicketRejectedNotification(Ticket ticket) {
        String msg = "Your ticket #" + ticket.getId() + " (" + ticket.getTitle() + ") has been rejected. Reason: "
                + ticket.getRejectionReason();
        buildAndSaveNotification(ticket.getReporter(), NotificationType.TICKET_REJECTED,
                "Ticket Rejected", msg, ReferenceType.TICKET, ticket.getId());
    }

    // --- Comment Notifications ---

    public void createNewCommentNotification(TicketComment comment, Ticket ticket) {
        Long authorId = comment.getAuthor().getId();
        Long ownerId = ticket.getReporter().getId();
        Long techId = ticket.getAssignedTechnician() != null ? ticket.getAssignedTechnician().getId() : null;

        // If commenter is NOT the ticket owner, notify the owner
        if (!authorId.equals(ownerId)) {
            String msg = "New comment on your ticket #" + ticket.getId() + " (" + ticket.getTitle() + ")";
            buildAndSaveNotification(ticket.getReporter(), NotificationType.NEW_COMMENT,
                    "New Comment", msg, ReferenceType.TICKET, ticket.getId());
        }

        // If ticket has assigned technician AND commenter is NOT the technician, notify
        // the tech
        if (techId != null && !authorId.equals(techId)) {
            String msg = "New comment on ticket #" + ticket.getId() + " (" + ticket.getTitle() + ") assigned to you";
            buildAndSaveNotification(ticket.getAssignedTechnician(), NotificationType.NEW_COMMENT,
                    "New Comment", msg, ReferenceType.TICKET, ticket.getId());
        }
    }

    // --- Helper ---

    private Notification buildAndSaveNotification(User recipient, NotificationType type, String title,
            String message, ReferenceType refType, Long refId) {
        Notification notification = Notification.builder()
                .user(recipient)
                .type(type)
                .title(title)
                .message(message)
                .referenceType(refType)
                .referenceId(refId)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        logger.info("Notification created: type={}, recipientEmail={}", type, recipient.getEmail());
        return saved;
    }
}
