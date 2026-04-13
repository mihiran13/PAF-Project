package com.smartcampus.dto.response;

import com.smartcampus.enums.TicketCategory;
import com.smartcampus.enums.TicketPriority;
import com.smartcampus.enums.TicketStatus;
import com.smartcampus.model.Ticket;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private Long id;

    private Long reporterId;
    private String reporterName;
    private String reporterEmail;

    private Long resourceId;
    private String resourceName;
    private String locationDescription;

    private TicketCategory category;
    private String title;
    private String description;
    private TicketPriority priority;
    private String preferredContact;
    private TicketStatus status;

    private Long assignedTechnicianId;
    private String assignedTechnicianName;
    private LocalDateTime assignedAt;
    private String assignedByName;

    private String resolutionNotes;
    private LocalDateTime resolvedAt;

    private String rejectionReason;
    private String rejectedByName;

    private LocalDateTime closedAt;

    private int attachmentCount;
    private int commentCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TicketResponse fromEntity(Ticket ticket) {
        if (ticket == null)
            return null;

        return TicketResponse.builder()
                .id(ticket.getId())
                .reporterId(ticket.getReporter() != null ? ticket.getReporter().getId() : null)
                .reporterName(ticket.getReporter() != null ? ticket.getReporter().getName() : null)
                .reporterEmail(ticket.getReporter() != null ? ticket.getReporter().getEmail() : null)
                .resourceId(ticket.getResource() != null ? ticket.getResource().getId() : null)
                .resourceName(ticket.getResource() != null ? ticket.getResource().getName() : null)
                .locationDescription(ticket.getLocationDescription())
                .category(ticket.getCategory())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .priority(ticket.getPriority())
                .preferredContact(ticket.getPreferredContact())
                .status(ticket.getStatus())
                .assignedTechnicianId(
                        ticket.getAssignedTechnician() != null ? ticket.getAssignedTechnician().getId() : null)
                .assignedTechnicianName(
                        ticket.getAssignedTechnician() != null ? ticket.getAssignedTechnician().getName() : null)
                .assignedAt(ticket.getAssignedAt())
                .assignedByName(ticket.getAssignedBy() != null ? ticket.getAssignedBy().getName() : null)
                .resolutionNotes(ticket.getResolutionNotes())
                .resolvedAt(ticket.getResolvedAt())
                .rejectionReason(ticket.getRejectionReason())
                .rejectedByName(ticket.getRejectedBy() != null ? ticket.getRejectedBy().getName() : null)
                .closedAt(ticket.getClosedAt())
                .attachmentCount(ticket.getAttachments() != null ? ticket.getAttachments().size() : 0)
                .commentCount(ticket.getComments() != null ? ticket.getComments().size() : 0)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
