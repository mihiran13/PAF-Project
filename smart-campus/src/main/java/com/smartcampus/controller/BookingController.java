package com.smartcampus.controller;

import com.smartcampus.dto.request.CancelBookingRequest;
import com.smartcampus.dto.request.CreateBookingRequest;
import com.smartcampus.dto.request.RejectBookingRequest;
import com.smartcampus.dto.request.VerifyBookingRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.BookingResponse;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.enums.BookingStatus;
import com.smartcampus.security.CurrentUser;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @CurrentUser CustomUserDetails currentUser) {

        BookingResponse response = bookingService.createBooking(request, currentUser.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking requested successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<BookingResponse>>> getMyBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @CurrentUser CustomUserDetails currentUser) {

        PagedResponse<BookingResponse> response = bookingService.getMyBookings(currentUser.getUser().getId(), status,
                page, size);
        return ResponseEntity.ok(ApiResponse.success("My bookings retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<BookingResponse>>> getAllBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<BookingResponse> response = bookingService.getAllBookings(status, resourceId, userId, startDate,
                endDate, page, size);
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @PathVariable Long id,
            @CurrentUser CustomUserDetails currentUser) {

        BookingResponse response = bookingService.getBookingById(id, currentUser.getUser());
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", response));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> approveBooking(
            @PathVariable Long id,
            @CurrentUser CustomUserDetails adminUser) {

        BookingResponse response = bookingService.approveBooking(id, adminUser.getUser());
        return ResponseEntity.ok(ApiResponse.success("Booking approved successfully", response));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> rejectBooking(
            @PathVariable Long id,
            @Valid @RequestBody RejectBookingRequest request,
            @CurrentUser CustomUserDetails adminUser) {

        BookingResponse response = bookingService.rejectBooking(id, request, adminUser.getUser());
        return ResponseEntity.ok(ApiResponse.success("Booking rejected successfully", response));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) CancelBookingRequest request,
            @CurrentUser CustomUserDetails currentUser) {

        if (request == null) {
            request = new CancelBookingRequest();
        }

        BookingResponse response = bookingService.cancelBooking(id, request, currentUser.getUser());
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // 204 No Content
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> verifyBooking(
            @PathVariable Long id,
            @Valid @RequestBody VerifyBookingRequest request) {

        BookingResponse response = bookingService.verifyBookingCheckIn(id, request.getToken());
        return ResponseEntity.ok(ApiResponse.success("Booking verified and user checked-in successfully", response));
    }
}
