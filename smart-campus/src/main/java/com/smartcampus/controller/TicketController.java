package com.smartcampus.controller;

import com.smartcampus.dto.request.AssignTechnicianRequest;
import com.smartcampus.dto.request.CreateTicketRequest;
import com.smartcampus.dto.request.RejectTicketRequest;
import com.smartcampus.dto.request.ResolveTicketRequest;
import com.smartcampus.dto.request.UpdateTicketStatusRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.dto.response.TicketDetailResponse;
import com.smartcampus.dto.response.TicketResponse;
import com.smartcampus.enums.TicketCategory;
import com.smartcampus.enums.TicketPriority;
import com.smartcampus.enums.TicketStatus;
import com.smartcampus.security.CurrentUser;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.AttachmentService;
import com.smartcampus.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final AttachmentService attachmentService;

    public TicketController(TicketService ticketService, AttachmentService attachmentService) {
        this.ticketService = ticketService;
        this.attachmentService = attachmentService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> createTicket(
            @RequestPart("ticket") @Valid CreateTicketRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @CurrentUser CustomUserDetails currentUser) {

        TicketResponse baseResponse = ticketService.createTicket(request, currentUser.getUser());

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                attachmentService.uploadAttachment(baseResponse.getId(), file, currentUser.getUser());
            }
        }

        // Fetch back full detail wrapping attachments that just finalized
        TicketDetailResponse fullDetail = ticketService.getTicketById(baseResponse.getId(), currentUser.getUser());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ticket created successfully", fullDetail));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<TicketResponse>>> getMyTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @CurrentUser CustomUserDetails currentUser) {

        PagedResponse<TicketResponse> response = ticketService.getMyTickets(currentUser.getUser().getId(), status, page,
                size);
        return ResponseEntity.ok(ApiResponse.success("My tickets retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<TicketResponse>>> getAllTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) Long technicianId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<TicketResponse> response = ticketService.getAllTickets(status, priority, category, technicianId,
                page, size);
        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved successfully", response));
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<TicketResponse>>> getAssignedTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @CurrentUser CustomUserDetails technician) {

        PagedResponse<TicketResponse> response = ticketService.getAssignedTickets(technician.getUser().getId(), status,
                page, size);
        return ResponseEntity.ok(ApiResponse.success("Assigned tickets retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> getTicketById(
            @PathVariable Long id,
            @CurrentUser CustomUserDetails currentUser) {

        TicketDetailResponse response = ticketService.getTicketById(id, currentUser.getUser());
        return ResponseEntity.ok(ApiResponse.success("Ticket details retrieved successfully", response));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TicketResponse>> assignTechnician(
            @PathVariable Long id,
            @Valid @RequestBody AssignTechnicianRequest request,
            @CurrentUser CustomUserDetails adminUser) {

        TicketResponse response = ticketService.assignTechnician(id, request, adminUser.getUser());
        return ResponseEntity.ok(ApiResponse.success("Technician assigned successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<TicketResponse>> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request,
            @CurrentUser CustomUserDetails currentUser) {

        TicketResponse response = ticketService.updateTicketStatus(id, request, currentUser.getUser());
        return ResponseEntity.ok(ApiResponse.success("Ticket status updated successfully", response));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<TicketResponse>> resolveTicket(
            @PathVariable Long id,
            @Valid @RequestBody ResolveTicketRequest request,
            @CurrentUser CustomUserDetails currentUser) {

        TicketResponse response = ticketService.resolveTicket(id, request, currentUser.getUser());
        return ResponseEntity.ok(ApiResponse.success("Ticket resolved successfully", response));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TicketResponse>> rejectTicket(
            @PathVariable Long id,
            @Valid @RequestBody RejectTicketRequest request,
            @CurrentUser CustomUserDetails adminUser) {

        TicketResponse response = ticketService.rejectTicket(id, request, adminUser.getUser());
        return ResponseEntity.ok(ApiResponse.success("Ticket rejected successfully", response));
    }
}
