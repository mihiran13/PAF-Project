package com.smartcampus.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity for uploading max 3 images (5MB limit) to contextualize Tickets.
 */
@Entity
@Table(name = "ticket_attachments", indexes = {
        @Index(name = "idx_attachment_ticket", columnList = "ticket_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 255, unique = true)
    private String storedFilename;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;
}
