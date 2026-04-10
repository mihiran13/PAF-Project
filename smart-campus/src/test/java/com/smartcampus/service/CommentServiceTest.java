package com.smartcampus.service;

import com.smartcampus.dto.request.CreateCommentRequest;
import com.smartcampus.dto.request.UpdateCommentRequest;
import com.smartcampus.dto.response.CommentResponse;
import com.smartcampus.enums.UserRole;
import com.smartcampus.exception.ForbiddenException;
import com.smartcampus.model.Ticket;
import com.smartcampus.model.TicketComment;
import com.smartcampus.model.User;
import com.smartcampus.repository.TicketCommentRepository;
import com.smartcampus.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private TicketCommentRepository commentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CommentService commentService;

    private User owner;
    private User tech;
    private User admin;
    private User randomUser;
    private Ticket ticket;
    private TicketComment comment;

    @BeforeEach
    void setUp() {
        owner = User.builder().role(UserRole.USER).build();
        owner.setId(1L);

        tech = User.builder().role(UserRole.TECHNICIAN).build();
        tech.setId(2L);

        admin = User.builder().role(UserRole.ADMIN).build();
        admin.setId(3L);

        randomUser = User.builder().role(UserRole.USER).build();
        randomUser.setId(4L);

        ticket = Ticket.builder()
                .reporter(owner)
                .assignedTechnician(tech)
                .build();
        ticket.setId(1L);

        comment = TicketComment.builder()
                .ticket(ticket)
                .author(owner)
                .content("Original Comment")
                .isEdited(false)
                .build();
        comment.setId(1L);
    }

    @Test
    void testAddComment_ByTicketOwner_Success() {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContent("Hello");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(commentRepository.save(any())).thenReturn(comment);

        CommentResponse response = commentService.addComment(1L, req, owner);

        assertNotNull(response);
        verify(notificationService).createNewCommentNotification(any(TicketComment.class), eq(ticket));
    }

    @Test
    void testAddComment_ByAssignedTechnician_Success() {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContent("Hello");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(commentRepository.save(any())).thenReturn(comment);

        CommentResponse response = commentService.addComment(1L, req, tech);
        assertNotNull(response);
    }

    @Test
    void testAddComment_ByAdmin_Success() {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContent("Hello");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(commentRepository.save(any())).thenReturn(comment);

        CommentResponse response = commentService.addComment(1L, req, admin);
        assertNotNull(response);
    }

    @Test
    void testAddComment_ByUnrelatedUser_ForbiddenException() {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContent("Hello");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(ForbiddenException.class, () -> commentService.addComment(1L, req, randomUser));
    }

    @Test
    void testUpdateComment_ByAuthor_Success() {
        UpdateCommentRequest req = new UpdateCommentRequest();
        req.setContent("Edited Comment");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any())).thenReturn(comment);

        commentService.updateComment(1L, req, owner);

        assertEquals("Edited Comment", comment.getContent());
        assertTrue(comment.getIsEdited());
    }

    @Test
    void testUpdateComment_ByNonAuthor_ForbiddenException() {
        UpdateCommentRequest req = new UpdateCommentRequest();
        req.setContent("Edited Comment");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThrows(ForbiddenException.class, () -> commentService.updateComment(1L, req, admin));
    }

    @Test
    void testDeleteComment_ByAuthor_Success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L, owner);

        verify(commentRepository).delete(comment);
    }

    @Test
    void testDeleteComment_ByAdmin_Success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L, admin);

        verify(commentRepository).delete(comment);
    }

    @Test
    void testDeleteComment_ByNonAuthorNonAdmin_ForbiddenException() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThrows(ForbiddenException.class, () -> commentService.deleteComment(1L, randomUser));
    }
}
