package com.smartcampus.repository;

import com.smartcampus.enums.BookingStatus;
import com.smartcampus.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByUserIdAndStatus(Long userId, BookingStatus status, Pageable pageable);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    Page<Booking> findByResourceId(Long resourceId, Pageable pageable);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndStatus(Long userId, BookingStatus status);

    long countByStatus(BookingStatus status);

    /**
     * Critical conflict checking query mapping the formula: existingStartTime <
     * newEndTime AND existingEndTime > newStartTime
     */
    @Query("SELECT b FROM Booking b WHERE b.resource.id = :resourceId AND b.bookingDate = :bookingDate " +
            "AND b.status IN :statuses " +
            "AND b.startTime < :endTime AND b.endTime > :startTime " +
            "AND (:excludeBookingId IS NULL OR b.id != :excludeBookingId)")
    List<Booking> findOverlappingBookings(
            @Param("resourceId") Long resourceId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("excludeBookingId") Long excludeBookingId);

    /**
     * Advanced filter query for admin oversight dashboards.
     */
    @Query("SELECT b FROM Booking b WHERE " +
            "(:status IS NULL OR b.status = :status) " +
            "AND (:resourceId IS NULL OR b.resource.id = :resourceId) " +
            "AND (:userId IS NULL OR b.user.id = :userId) " +
            "AND (:startDate IS NULL OR b.bookingDate >= :startDate) " +
            "AND (:endDate IS NULL OR b.bookingDate <= :endDate)")
    Page<Booking> findAdvancedBookings(
            @Param("status") BookingStatus status,
            @Param("resourceId") Long resourceId,
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}
