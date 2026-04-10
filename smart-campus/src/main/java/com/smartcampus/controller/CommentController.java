package com.smartcampus.controller;

import com.smartcampus.dto.request.CreateCommentRequest;
import com.smartcampus.dto.request.UpdateCommentRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.CommentResponse;
import com.smartcampus.security.CurrentUser;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/tickets/{ticketId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateCommentRequest request,
            @CurrentUser CustomUserDetails currentUser) {

        CommentResponse response = commentService.addComment(ticketId, request, currentUser.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added successfully", response));
    }

    @GetMapping("/tickets/{ticketId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> listComments(
            @PathVariable Long ticketId,
            @CurrentUser CustomUserDetails currentUser) {

        List<CommentResponse> response = commentService.getCommentsByTicketId(ticketId, currentUser.getUser());
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", response));
    }

    @PutMapping("/comments/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommentResponse>> editComment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCommentRequest request,
            @CurrentUser CustomUserDetails currentUser) {

        CommentResponse response = commentService.updateComment(id, request, currentUser.getUser());
        return ResponseEntity.ok(ApiResponse.success("Comment updated successfully", response));
    }

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            @CurrentUser CustomUserDetails currentUser) {

        commentService.deleteComment(id, currentUser.getUser());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
