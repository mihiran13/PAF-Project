package com.smartcampus.service;

import com.smartcampus.dto.request.CreateCommentRequest;
import com.smartcampus.dto.request.UpdateCommentRequest;
import com.smartcampus.dto.response.CommentResponse;
import com.smartcampus.enums.UserRole;
import com.smartcampus.exception.ForbiddenException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.Ticket;
import com.smartcampus.model.TicketComment;
import com.smartcampus.model.User;
import com.smartcampus.repository.TicketCommentRepository;
import com.smartcampus.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentService {

    private final TicketCommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;

    @Autowired
    public CommentService(TicketCommentRepository commentRepository, TicketRepository ticketRepository,
            NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.notificationService = notificationService;
    }

    public CommentResponse addComment(Long ticketId, CreateCommentRequest request, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        if (!hasTicketAccess(ticket, currentUser)) {
            throw new ForbiddenException("You do not have access to comment on this ticket");
        }

        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .author(currentUser)
                .content(request.getContent())
                .isEdited(false)
                .build();

        TicketComment saved = commentRepository.save(comment);

        notificationService.createNewCommentNotification(saved, ticket);

        CommentResponse res = CommentResponse.fromEntity(saved);
        res.setCanEdit(true);
        res.setCanDelete(true);
        return res;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTicketId(Long ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        if (!hasTicketAccess(ticket, currentUser)) {
            throw new ForbiddenException("You do not have access to view this ticket's comments");
        }

        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(c -> {
                    CommentResponse res = CommentResponse.fromEntity(c);
                    boolean isAuthor = c.getAuthor().getId().equals(currentUser.getId());
                    res.setCanEdit(isAuthor);
                    res.setCanDelete(isAuthor || currentUser.getRole() == UserRole.ADMIN);
                    return res;
                })
                .collect(Collectors.toList());
    }

    public CommentResponse updateComment(Long commentId, UpdateCommentRequest request, User currentUser) {
        TicketComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketComment", "id", commentId));

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only edit your own comments");
        }

        comment.setContent(request.getContent());
        comment.setIsEdited(true);
        TicketComment saved = commentRepository.save(comment);

        CommentResponse res = CommentResponse.fromEntity(saved);
        res.setCanEdit(true);
        res.setCanDelete(true);
        return res;
    }

    public void deleteComment(Long commentId, User currentUser) {
        TicketComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketComment", "id", commentId));

        if (!comment.getAuthor().getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    private boolean hasTicketAccess(Ticket ticket, User user) {
        if (user.getRole() == UserRole.ADMIN)
            return true;
        if (ticket.getReporter().getId().equals(user.getId()))
            return true;
        return ticket.getAssignedTechnician() != null && ticket.getAssignedTechnician().getId().equals(user.getId());
    }
}
