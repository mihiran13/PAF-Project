package com.smartcampus.service;

import com.smartcampus.dto.request.CancelBookingRequest;
import com.smartcampus.dto.request.CreateBookingRequest;
import com.smartcampus.dto.request.RejectBookingRequest;
import com.smartcampus.dto.response.BookingResponse;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.enums.BookingStatus;
import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.UserRole;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ConflictException;
import com.smartcampus.exception.ForbiddenException;
import com.smartcampus.exception.InvalidStateTransitionException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.Booking;
import com.smartcampus.model.Resource;
import com.smartcampus.model.ResourceAvailabilityWindow;
import com.smartcampus.model.User;
import com.smartcampus.repository.BookingRepository;
import com.smartcampus.repository.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final NotificationService notificationService;

    @Autowired
    public BookingService(BookingRepository bookingRepository, ResourceRepository resourceRepository,
            NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.resourceRepository = resourceRepository;
        this.notificationService = notificationService;
    }

    public BookingResponse createBooking(CreateBookingRequest request, User currentUser) {
        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", request.getResourceId()));

        if (resource.getStatus() == ResourceStatus.OUT_OF_SERVICE) {
            throw new BadRequestException("Cannot book a resource that is out of service");
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        if (request.getBookingDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Booking date must be today or in the future");
        }

        validateAvailabilityWindow(resource, request.getBookingDate(), request.getStartTime(), request.getEndTime());

        List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.PENDING, BookingStatus.APPROVED);
        List<Booking> conflicts = bookingRepository.findOverlappingBookings(
                resource.getId(), request.getBookingDate(), request.getStartTime(), request.getEndTime(),
                activeStatuses, null);

        if (!conflicts.isEmpty()) {
            Map<String, Object> details = new HashMap<>();
            details.put("overlappingCount", conflicts.size());
            throw new ConflictException("Booking conflicts with existing booking(s)", details);
        }

        Booking booking = Booking.builder()
                .resource(resource)
                .user(currentUser)
                .bookingDate(request.getBookingDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .purpose(request.getPurpose())
                .expectedAttendees(request.getExpectedAttendees())
                .status(BookingStatus.PENDING)
                .build();

        Booking saved = bookingRepository.save(booking);
        logger.info("Booking created by user {} for resource {} on {}", currentUser.getId(), resource.getId(),
                saved.getBookingDate());

        return BookingResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getMyBookings(Long userId, BookingStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingsPage;

        if (status != null) {
            bookingsPage = bookingRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            bookingsPage = bookingRepository.findByUserId(userId, pageable);
        }

        return PagedResponse.from(bookingsPage.map(BookingResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getAllBookings(BookingStatus status, Long resourceId, Long userId,
            LocalDate startDate, LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingsPage = bookingRepository.findAdvancedBookings(status, resourceId, userId, startDate,
                endDate, pageable);
        return PagedResponse.from(bookingsPage.map(BookingResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId, User currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (!booking.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("You can only view your own bookings");
        }

        return BookingResponse.fromEntity(booking);
    }

    public BookingResponse approveBooking(Long bookingId, User adminUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidStateTransitionException("Booking", booking.getStatus().name(),
                    BookingStatus.APPROVED.name());
        }

        List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.PENDING, BookingStatus.APPROVED);
        List<Booking> conflicts = bookingRepository.findOverlappingBookings(
                booking.getResource().getId(), booking.getBookingDate(),
                booking.getStartTime(), booking.getEndTime(),
                activeStatuses, booking.getId());

        if (!conflicts.isEmpty()) {
            throw new ConflictException("Cannot approve: booking now conflicts with another approved booking");
        }

        booking.setStatus(BookingStatus.APPROVED);
        booking.setReviewedBy(adminUser);
        booking.setReviewedAt(LocalDateTime.now());
        
        // Generate Secure QR Verification Token
        booking.setVerificationToken(java.util.UUID.randomUUID().toString());
        
        bookingRepository.save(booking);

        logger.info("Booking {} approved by admin {}", bookingId, adminUser.getId());

        notificationService.createBookingApprovedNotification(booking);

        return BookingResponse.fromEntity(booking);
    }

    public BookingResponse rejectBooking(Long bookingId, RejectBookingRequest request, User adminUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidStateTransitionException("Booking", booking.getStatus().name(),
                    BookingStatus.REJECTED.name());
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setRejectionReason(request.getRejectionReason());
        booking.setReviewedBy(adminUser);
        booking.setReviewedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        logger.info("Booking {} rejected by admin {}. Reason: {}", bookingId, adminUser.getId(),
                request.getRejectionReason());

        notificationService.createBookingRejectedNotification(booking);

        return BookingResponse.fromEntity(booking);
    }

    public BookingResponse cancelBooking(Long bookingId, CancelBookingRequest request, User currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new InvalidStateTransitionException("Booking", booking.getStatus().name(),
                    BookingStatus.CANCELLED.name());
        }

        if (!booking.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("You can only cancel your own bookings");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        if (request != null && request.getCancellationReason() != null) {
            booking.setCancellationReason(request.getCancellationReason());
        }
        booking.setCancelledBy(currentUser);
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepository.save(booking);

        logger.info("Booking {} cancelled by user {}", bookingId, currentUser.getId());

        notificationService.createBookingCancelledNotification(booking);

        return BookingResponse.fromEntity(booking);
    }

    public void deleteBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        bookingRepository.delete(booking);
        logger.info("Booking {} deleted by admin", bookingId);
    }

    public BookingResponse verifyBookingCheckIn(Long bookingId, String token) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new BadRequestException("This booking is not in APPROVED status. Current: " + booking.getStatus());
        }

        if (booking.getIsCheckedIn()) {
            throw new BadRequestException("User has already checked in for this booking at " + booking.getCheckedInAt());
        }

        if (booking.getVerificationToken() == null || !booking.getVerificationToken().equals(token)) {
            throw new ForbiddenException("Invalid verification token for this booking.");
        }

        // Logic Check: Is it the right day?
        LocalDate today = LocalDate.now();
        if (!booking.getBookingDate().equals(today)) {
            throw new BadRequestException("Check-in failed: Booking is for " + booking.getBookingDate() + ", but today is " + today);
        }

        // Logic Check: Is it too early? (e.g. max 15 mins before)
        LocalTime now = LocalTime.now();
        if (now.isBefore(booking.getStartTime().minusMinutes(15))) {
            throw new BadRequestException("Check-in failed: Too early. Booking starts at " + booking.getStartTime());
        }

        // Logic Check: Is it too late? (Booking ended)
        if (now.isAfter(booking.getEndTime())) {
            throw new BadRequestException("Check-in failed: Booking has already ended at " + booking.getEndTime());
        }

        booking.setIsCheckedIn(true);
        booking.setCheckedInAt(LocalDateTime.now());
        bookingRepository.save(booking);

        logger.info("Booking {} checked-in successfully via QR verification", bookingId);
        return BookingResponse.fromEntity(booking);
    }

    private void validateAvailabilityWindow(Resource resource, LocalDate bookingDate, LocalTime startTime,
            LocalTime endTime) {
        List<ResourceAvailabilityWindow> windows = resource.getAvailabilityWindows();

        if (windows == null || windows.isEmpty()) {
            return;
        }

        DayOfWeek requiredDay = bookingDate.getDayOfWeek();
        boolean matchesWindow = windows.stream()
                .filter(w -> w.getDayOfWeek() == requiredDay)
                .anyMatch(w -> !startTime.isBefore(w.getStartTime()) && !endTime.isAfter(w.getEndTime()));

        if (!matchesWindow) {
            throw new BadRequestException(
                    "Requested time range is outside resource availability hours for " + requiredDay.name());
        }
    }
}
