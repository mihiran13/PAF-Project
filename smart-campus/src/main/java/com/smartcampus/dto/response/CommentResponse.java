package com.smartcampus.dto.response;

import com.smartcampus.enums.UserRole;
import com.smartcampus.model.TicketComment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private Long ticketId;

    private Long authorId;
    private String authorName;
    private String authorEmail;
    private UserRole authorRole;

    private String content;
    private Boolean isEdited;

    private Boolean canEdit;
    private Boolean canDelete;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentResponse fromEntity(TicketComment comment) {
        if (comment == null)
            return null;

        return CommentResponse.builder()
                .id(comment.getId())
                .ticketId(comment.getTicket() != null ? comment.getTicket().getId() : null)
                .authorId(comment.getAuthor() != null ? comment.getAuthor().getId() : null)
                .authorName(comment.getAuthor() != null ? comment.getAuthor().getName() : null)
                .authorEmail(comment.getAuthor() != null ? comment.getAuthor().getEmail() : null)
                .authorRole(comment.getAuthor() != null ? comment.getAuthor().getRole() : null)
                .content(comment.getContent())
                .isEdited(comment.getIsEdited())
                .canEdit(false) // Set actively by Service logic
                .canDelete(false) // Set actively by Service logic
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
