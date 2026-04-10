package com.smartcampus.dto.response;

import com.smartcampus.model.Ticket;
import com.smartcampus.model.TicketAttachment;
import com.smartcampus.model.TicketComment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailResponse extends TicketResponse {

    private List<AttachmentResponse> attachments;
    private List<CommentResponse> comments;

    public static TicketDetailResponse fromEntity(Ticket ticket, List<TicketAttachment> attachments,
            List<TicketComment> comments) {
        if (ticket == null)
            return null;

        // Base mappings
        TicketResponse base = TicketResponse.fromEntity(ticket);

        TicketDetailResponse detail = new TicketDetailResponse();
        // Manually map fields from base because Lombok builder doesn't automatically
        // inherit well for complex builder patterns
        detail.setId(base.getId());
        detail.setReporterId(base.getReporterId());
        detail.setReporterName(base.getReporterName());
        detail.setReporterEmail(base.getReporterEmail());
        detail.setResourceId(base.getResourceId());
        detail.setResourceName(base.getResourceName());
        detail.setLocationDescription(base.getLocationDescription());
        detail.setCategory(base.getCategory());
        detail.setTitle(base.getTitle());
        detail.setDescription(base.getDescription());
        detail.setPriority(base.getPriority());
        detail.setPreferredContact(base.getPreferredContact());
        detail.setStatus(base.getStatus());
        detail.setAssignedTechnicianId(base.getAssignedTechnicianId());
        detail.setAssignedTechnicianName(base.getAssignedTechnicianName());
        detail.setAssignedAt(base.getAssignedAt());
        detail.setAssignedByName(base.getAssignedByName());
        detail.setResolutionNotes(base.getResolutionNotes());
        detail.setResolvedAt(base.getResolvedAt());
        detail.setRejectionReason(base.getRejectionReason());
        detail.setRejectedByName(base.getRejectedByName());
        detail.setClosedAt(base.getClosedAt());
        detail.setAttachmentCount(base.getAttachmentCount());
        detail.setCommentCount(base.getCommentCount());
        detail.setCreatedAt(base.getCreatedAt());
        detail.setUpdatedAt(base.getUpdatedAt());

        // Sub entity lists
        List<AttachmentResponse> mappedAttachments = attachments == null ? Collections.emptyList()
                : attachments.stream().map(AttachmentResponse::fromEntity).collect(Collectors.toList());

        List<CommentResponse> mappedComments = comments == null ? Collections.emptyList()
                : comments.stream().map(CommentResponse::fromEntity).collect(Collectors.toList());

        detail.setAttachments(mappedAttachments);
        detail.setComments(mappedComments);

        return detail;
    }
}
