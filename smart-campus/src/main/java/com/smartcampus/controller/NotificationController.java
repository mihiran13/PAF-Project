package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.NotificationResponse;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.dto.response.UnreadCountResponse;
import com.smartcampus.security.CurrentUser;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getUserNotifications(
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @CurrentUser CustomUserDetails currentUser) {

        PagedResponse<NotificationResponse> response = notificationService.getUserNotifications(
                currentUser.getUser().getId(), isRead, page, size);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", response));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(@CurrentUser CustomUserDetails currentUser) {
        long count = notificationService.getUnreadCount(currentUser.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved", new UnreadCountResponse(count)));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @CurrentUser CustomUserDetails currentUser) {

        NotificationResponse response = notificationService.markAsRead(id, currentUser.getUser());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", response));
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> markAllAsRead(@CurrentUser CustomUserDetails currentUser) {
        notificationService.markAllAsRead(currentUser.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }
}
