package com.smartcampus.repository;

import com.smartcampus.model.TicketComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    Page<TicketComment> findByTicketId(Long ticketId, Pageable pageable);

    long countByTicketId(Long ticketId);
}
