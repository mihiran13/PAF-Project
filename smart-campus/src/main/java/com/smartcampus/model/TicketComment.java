package com.smartcampus.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Chat/Communication layer mapped safely inside an active Ticket thread.
 */
@Entity
@Table(name = "ticket_comments", indexes = {
        @Index(name = "idx_comment_ticket", columnList = "ticket_id"),
        @Index(name = "idx_comment_author", columnList = "author_id"),
        @Index(name = "idx_comment_ticket_created", columnList = "ticket_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isEdited = false;
}
