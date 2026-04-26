package com.smartcampus.dto.response;

import com.smartcampus.enums.BookingStatus;
import com.smartcampus.enums.ResourceType;
import com.smartcampus.model.Booking;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class BookingResponse {
    private Long id;

    // Extracted safely from Proxy Entity
    private Long resourceId;
    private String resourceName;
    private ResourceType resourceType;
    private String resourceLocation;

    private Long userId;
    private String userName;
    private String userEmail;

    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String purpose;
    private Integer expectedAttendees;
    private BookingStatus status;
    private String rejectionReason;
    private String cancellationReason;

    private String reviewedByName;
    private LocalDateTime reviewedAt;

    private String cancelledByName;
    private LocalDateTime cancelledAt;

    private String verificationToken;
    private Boolean isCheckedIn;
    private LocalDateTime checkedInAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BookingResponse fromEntity(Booking booking) {
        if (booking == null)
            return null;

        return BookingResponse.builder()
                .id(booking.getId())
                .resourceId(booking.getResource() != null ? booking.getResource().getId() : null)
                .resourceName(booking.getResource() != null ? booking.getResource().getName() : null)
                .resourceType(booking.getResource() != null ? booking.getResource().getType() : null)
                .resourceLocation(booking.getResource() != null ? booking.getResource().getLocation() : null)
                .userId(booking.getUser() != null ? booking.getUser().getId() : null)
                .userName(booking.getUser() != null ? booking.getUser().getName() : null)
                .userEmail(booking.getUser() != null ? booking.getUser().getEmail() : null)
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .purpose(booking.getPurpose())
                .expectedAttendees(booking.getExpectedAttendees())
                .status(booking.getStatus())
                .rejectionReason(booking.getRejectionReason())
                .cancellationReason(booking.getCancellationReason())
                .reviewedByName(booking.getReviewedBy() != null ? booking.getReviewedBy().getName() : null)
                .reviewedAt(booking.getReviewedAt())
                .cancelledByName(booking.getCancelledBy() != null ? booking.getCancelledBy().getName() : null)
                .cancelledAt(booking.getCancelledAt())
                .verificationToken(booking.getVerificationToken())
                .isCheckedIn(booking.getIsCheckedIn())
                .checkedInAt(booking.getCheckedInAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
