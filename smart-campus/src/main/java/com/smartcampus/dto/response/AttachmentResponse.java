package com.smartcampus.dto.response;

import com.smartcampus.model.TicketAttachment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AttachmentResponse {
    private Long id;
    private Long ticketId;
    private String originalFilename;
    private String storedFilename;
    private String contentType;
    private Long fileSize;
    private String uploadedByName;
    private Long uploadedById;
    private LocalDateTime createdAt;

    private String downloadUrl;

    public static AttachmentResponse fromEntity(TicketAttachment attachment) {
        if (attachment == null)
            return null;

        return AttachmentResponse.builder()
                .id(attachment.getId())
                .ticketId(attachment.getTicket() != null ? attachment.getTicket().getId() : null)
                .originalFilename(attachment.getOriginalFilename())
                .storedFilename(attachment.getStoredFilename())
                .contentType(attachment.getContentType())
                .fileSize(attachment.getFileSize())
                .uploadedByName(attachment.getUploadedBy() != null ? attachment.getUploadedBy().getName() : null)
                .uploadedById(attachment.getUploadedBy() != null ? attachment.getUploadedBy().getId() : null)
                .createdAt(attachment.getCreatedAt())
                .downloadUrl("/api/attachments/" + attachment.getId() + "/download")
                .build();
    }
}
