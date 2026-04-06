package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.AttachmentResponse;
import com.smartcampus.security.CurrentUser;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.AttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping("/tickets/{ticketId}/attachments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadAttachment(
            @PathVariable Long ticketId,
            @RequestParam("file") MultipartFile file,
            @CurrentUser CustomUserDetails currentUser) {

        AttachmentResponse response = attachmentService.uploadAttachment(ticketId, file, currentUser.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attachment uploaded successfully", response));
    }

    @GetMapping("/tickets/{ticketId}/attachments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> listAttachments(
            @PathVariable Long ticketId,
            @CurrentUser CustomUserDetails currentUser) {

        List<AttachmentResponse> response = attachmentService.getAttachmentsByTicketId(ticketId, currentUser.getUser());
        return ResponseEntity.ok(ApiResponse.success("Attachments retrieved successfully", response));
    }

    @GetMapping("/attachments/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long id,
            @CurrentUser CustomUserDetails currentUser) {

        Resource file = attachmentService.downloadAttachment(id, currentUser.getUser());

        // Technically this metadata should come from DB attachment record directly, but
        // resolving minimally
        String filename = file.getFilename();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(file);
    }

    @DeleteMapping("/attachments/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable Long id,
            @CurrentUser CustomUserDetails currentUser) {

        attachmentService.deleteAttachment(id, currentUser.getUser());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
