package com.smartcampus.service;

import com.smartcampus.dto.request.CancelBookingRequest;
import com.smartcampus.dto.request.CreateBookingRequest;
import com.smartcampus.dto.request.RejectBookingRequest;
import com.smartcampus.dto.response.BookingResponse;
import com.smartcampus.enums.BookingStatus;
import com.smartcampus.enums.ResourceStatus;
import com.smartcampus.enums.UserRole;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ConflictException;
import com.smartcampus.exception.ForbiddenException;
import com.smartcampus.exception.InvalidStateTransitionException;
import com.smartcampus.model.Booking;
import com.smartcampus.model.Resource;
import com.smartcampus.model.ResourceAvailabilityWindow;
import com.smartcampus.model.User;
import com.smartcampus.repository.BookingRepository;
import com.smartcampus.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private User admin;
    private Resource activeResource;
    private Booking pendingBooking;
    private LocalDate futureDate;
    private CreateBookingRequest createReq;

    @BeforeEach
    void setUp() {
        user = User.builder().role(UserRole.USER).build();
        user.setId(1L);

        admin = User.builder().role(UserRole.ADMIN).build();
        admin.setId(2L);

        ResourceAvailabilityWindow window = ResourceAvailabilityWindow.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();
        window.setId(1L);

        List<ResourceAvailabilityWindow> list = new ArrayList<>();
        list.add(window);

        activeResource = Resource.builder()
                .status(ResourceStatus.ACTIVE)
                .availabilityWindows(list)
                .build();
        activeResource.setId(1L);
        // Ensure parent linkage locally
        window.setResource(activeResource);

        futureDate = LocalDate.now().plusDays(7);
        while (futureDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            futureDate = futureDate.plusDays(1);
        }

        pendingBooking = Booking.builder()
                .resource(activeResource)
                .user(user)
                .bookingDate(futureDate)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .status(BookingStatus.PENDING)
                .build();
        pendingBooking.setId(1L);

        createReq = new CreateBookingRequest();
        createReq.setResourceId(1L);
        createReq.setBookingDate(futureDate);
        createReq.setStartTime(LocalTime.of(10, 0));
        createReq.setEndTime(LocalTime.of(12, 0));
        createReq.setPurpose("Event");
    }

    @Test
    void testCreateBooking_Valid_SuccessPending() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(activeResource));
        when(bookingRepository.findOverlappingBookings(eq(1L), eq(futureDate), any(), any(), any(), eq(null)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any())).thenReturn(pendingBooking);

        BookingResponse response = bookingService.createBooking(createReq, user);

        assertNotNull(response);
        assertEquals(BookingStatus.PENDING, pendingBooking.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_InactiveResource_BadRequestException() {
        activeResource.setStatus(ResourceStatus.OUT_OF_SERVICE);
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(activeResource));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(createReq, user));
    }

    @Test
    void testCreateBooking_PastDate_BadRequestException() {
        createReq.setBookingDate(LocalDate.now().minusDays(1));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(activeResource));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(createReq, user));
    }

    @Test
    void testCreateBooking_StartAfterEnd_BadRequestException() {
        createReq.setStartTime(LocalTime.of(14, 0));
        createReq.setEndTime(LocalTime.of(12, 0));
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(activeResource));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(createReq, user));
    }

    @Test
    void testCreateBooking_Overlap_ConflictException() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(activeResource));
        // Simulate overlap
        when(bookingRepository.findOverlappingBookings(eq(1L), eq(futureDate), any(), any(), any(), eq(null)))
                .thenReturn(List.of(pendingBooking)); 

        assertThrows(ConflictException.class, () -> bookingService.createBooking(createReq, user));
    }

    @Test
    void testApproveBooking_Pending_Approved() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        when(bookingRepository.findOverlappingBookings(eq(1L), eq(futureDate), any(), any(), any(), eq(1L)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any())).thenReturn(pendingBooking);

        bookingService.approveBooking(1L, admin);

        assertEquals(BookingStatus.APPROVED, pendingBooking.getStatus());
        verify(notificationService).createBookingApprovedNotification(pendingBooking);
    }

    @Test
    void testApproveBooking_NonPending_InvalidStateTransitionException() {
        pendingBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));

        assertThrows(InvalidStateTransitionException.class, () -> bookingService.approveBooking(1L, admin));
    }

    @Test
    void testApproveBooking_ConflictAtApproval_ConflictException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        when(bookingRepository.findOverlappingBookings(eq(1L), eq(futureDate), any(), any(), any(), eq(1L)))
                .thenReturn(List.of(new Booking()));

        assertThrows(ConflictException.class, () -> bookingService.approveBooking(1L, admin));
    }

    @Test
    void testRejectBooking_PendingWithReason_Rejected() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        when(bookingRepository.save(any())).thenReturn(pendingBooking);

        RejectBookingRequest rejectReq = new RejectBookingRequest();
        rejectReq.setRejectionReason("Unavailable");

        bookingService.rejectBooking(1L, rejectReq, admin);

        assertEquals(BookingStatus.REJECTED, pendingBooking.getStatus());
        assertEquals("Unavailable", pendingBooking.getRejectionReason());
        verify(notificationService).createBookingRejectedNotification(pendingBooking);
    }

    @Test
    void testRejectBooking_NonPending_InvalidStateTransitionException() {
        pendingBooking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));

        RejectBookingRequest rejectReq = new RejectBookingRequest();
        rejectReq.setRejectionReason("Error");

        assertThrows(InvalidStateTransitionException.class, () -> bookingService.rejectBooking(1L, rejectReq, admin));
    }

    @Test
    void testCancelBooking_ApprovedByOwner_Cancelled() {
        pendingBooking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));
        when(bookingRepository.save(any())).thenReturn(pendingBooking);

        CancelBookingRequest cancelReq = new CancelBookingRequest();
        cancelReq.setCancellationReason("No need");

        bookingService.cancelBooking(1L, cancelReq, user);

        assertEquals(BookingStatus.CANCELLED, pendingBooking.getStatus());
        assertEquals("No need", pendingBooking.getCancellationReason());
        verify(notificationService).createBookingCancelledNotification(pendingBooking);
    }

    @Test
    void testCancelBooking_NonApproved_InvalidStateTransitionException() {
        pendingBooking.setStatus(BookingStatus.REJECTED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));

        assertThrows(InvalidStateTransitionException.class,
                () -> bookingService.cancelBooking(1L, new CancelBookingRequest(), user));
    }

    @Test
    void testCancelBooking_ByNonOwner_ForbiddenException() {
        pendingBooking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));

        User other = User.builder().role(UserRole.USER).build();
        other.setId(99L);

        assertThrows(ForbiddenException.class,
                () -> bookingService.cancelBooking(1L, new CancelBookingRequest(), other));
    }

    @Test
    void testGetBookingById_ByOwner_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));

        BookingResponse response = bookingService.getBookingById(1L, user);
        assertNotNull(response);
    }

    @Test
    void testGetBookingById_ByNonOwner_ForbiddenException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(pendingBooking));

        User other = User.builder().role(UserRole.USER).build();
        other.setId(99L);

        assertThrows(ForbiddenException.class, () -> bookingService.getBookingById(1L, other));
    }
}
