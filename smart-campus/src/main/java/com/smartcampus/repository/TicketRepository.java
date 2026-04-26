package com.smartcampus.repository;

import com.smartcampus.enums.TicketCategory;
import com.smartcampus.enums.TicketPriority;
import com.smartcampus.enums.TicketStatus;
import com.smartcampus.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Page<Ticket> findByReporterId(Long reporterId, Pageable pageable);

    Page<Ticket> findByReporterIdAndStatus(Long reporterId, TicketStatus status, Pageable pageable);

    Page<Ticket> findByAssignedTechnicianId(Long technicianId, Pageable pageable);

    Page<Ticket> findByAssignedTechnicianIdAndStatus(Long technicianId, TicketStatus status, Pageable pageable);

    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    long countByStatus(TicketStatus status);

    long countByAssignedTechnicianIdAndStatus(Long technicianId, TicketStatus status);

    @Query("SELECT t FROM Ticket t WHERE " +
            "(:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:category IS NULL OR t.category = :category) " +
            "AND (:technicianId IS NULL OR t.assignedTechnician.id = :technicianId) " +
            "AND (:reporterId IS NULL OR t.reporter.id = :reporterId)")
    Page<Ticket> findAdvancedTickets(
            @Param("status") TicketStatus status,
            @Param("priority") TicketPriority priority,
            @Param("category") TicketCategory category,
            @Param("technicianId") Long technicianId,
            @Param("reporterId") Long reporterId,
            Pageable pageable);
}
