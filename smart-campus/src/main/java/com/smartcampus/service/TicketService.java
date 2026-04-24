package com.smartcampus.service;

import com.smartcampus.dto.request.AssignTechnicianRequest;
import com.smartcampus.dto.request.CreateTicketRequest;
import com.smartcampus.dto.request.RejectTicketRequest;
import com.smartcampus.dto.request.ResolveTicketRequest;
import com.smartcampus.dto.request.UpdateTicketStatusRequest;
import com.smartcampus.dto.response.CommentResponse;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.dto.response.TicketDetailResponse;
import com.smartcampus.dto.response.TicketResponse;
import com.smartcampus.enums.TicketCategory;
import com.smartcampus.enums.TicketPriority;
import com.smartcampus.enums.TicketStatus;
import com.smartcampus.enums.UserRole;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ForbiddenException;
import com.smartcampus.exception.InvalidStateTransitionException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.Resource;
import com.smartcampus.model.Ticket;
import com.smartcampus.model.TicketAttachment;
import com.smartcampus.model.TicketComment;
import com.smartcampus.model.User;
import com.smartcampus.repository.ResourceRepository;
import com.smartcampus.repository.TicketAttachmentRepository;
import com.smartcampus.repository.TicketCommentRepository;
import com.smartcampus.repository.TicketRepository;
import com.smartcampus.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final TicketAttachmentRepository attachmentRepository;
    private final TicketCommentRepository commentRepository;
    private final NotificationService notificationService;

    @Autowired
    public TicketService(TicketRepository ticketRepository, ResourceRepository resourceRepository,
            UserRepository userRepository, TicketAttachmentRepository attachmentRepository,
            TicketCommentRepository commentRepository, NotificationService notificationService) {
        this.ticketRepository = ticketRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.attachmentRepository = attachmentRepository;
        this.commentRepository = commentRepository;
        this.notificationService = notificationService;
    }

    public TicketResponse createTicket(CreateTicketRequest request, User currentUser) {
        if (request.getResourceId() == null
                && (request.getLocationDescription() == null || request.getLocationDescription().trim().isEmpty())) {
            throw new BadRequestException("Either a Resource ID or a Location Description must be provided");
        }

        Resource resource = null;
        if (request.getResourceId() != null) {
            resource = resourceRepository.findById(request.getResourceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", request.getResourceId()));
        }

        Ticket ticket = Ticket.builder()
                .reporter(currentUser)
                .resource(resource)
                .locationDescription(request.getLocationDescription())
                .category(request.getCategory())
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .preferredContact(request.getPreferredContact())
                .status(TicketStatus.OPEN)
                .build();

        Ticket saved = ticketRepository.save(ticket);
        logger.info("Ticket {} created by user {}", saved.getId(), currentUser.getId());
        return TicketResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<TicketResponse> getMyTickets(Long userId, TicketStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Ticket> tickets = status != null ? ticketRepository.findByReporterIdAndStatus(userId, status, pageable)
                : ticketRepository.findByReporterId(userId, pageable);
        return PagedResponse.from(tickets.map(TicketResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public PagedResponse<TicketResponse> getAllTickets(TicketStatus status, TicketPriority priority,
            TicketCategory category, Long technicianId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Ticket> tickets = ticketRepository.findAdvancedTickets(status, priority, category, technicianId, null,
                pageable);
        return PagedResponse.from(tickets.map(TicketResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public PagedResponse<TicketResponse> getAssignedTickets(Long technicianId, TicketStatus status, int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Ticket> tickets = status != null
                ? ticketRepository.findByAssignedTechnicianIdAndStatus(technicianId, status, pageable)
                : ticketRepository.findByAssignedTechnicianId(technicianId, pageable);
        return PagedResponse.from(tickets.map(TicketResponse::fromEntity));
    }

    @Transactional(readOnly = true)
    public TicketDetailResponse getTicketById(Long ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        if (!hasTicketAccess(ticket, currentUser)) {
            throw new ForbiddenException("You do not have access to view this ticket");
        }

        List<TicketAttachment> attachments = attachmentRepository.findByTicketId(ticketId);
        List<TicketComment> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);

        TicketDetailResponse response = TicketDetailResponse.fromEntity(ticket, attachments, comments);

        if (response.getComments() != null) {
            for (CommentResponse c : response.getComments()) {
                boolean isAuthor = c.getAuthorId().equals(currentUser.getId());
                boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
                c.setCanEdit(isAuthor);
                c.setCanDelete(isAuthor || isAdmin);
            }
        }

        return response;
    }

    public TicketResponse assignTechnician(Long ticketId, AssignTechnicianRequest request, User adminUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        User technician = userRepository.findById(request.getTechnicianUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getTechnicianUserId()));

        if (technician.getRole() != UserRole.TECHNICIAN) {
            throw new BadRequestException("User is not a technician");
        }

        ticket.setAssignedTechnician(technician);
        ticket.setAssignedAt(LocalDateTime.now());
        ticket.setAssignedBy(adminUser);
        Ticket saved = ticketRepository.save(ticket);

        logger.info("Ticket {} assigned to technician {}", ticketId, technician.getId());

        notificationService.createTicketAssignedNotification(ticket);

        return TicketResponse.fromEntity(saved);
    }

    public TicketResponse updateTicketStatus(Long ticketId, UpdateTicketStatusRequest request, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        TicketStatus currentStatus = ticket.getStatus();
        TicketStatus targetStatus = request.getStatus();

        if (targetStatus == TicketStatus.RESOLVED) {
            throw new BadRequestException("Use /resolve endpoint to resolve tickets, resolution notes are required");
        }

        if (currentStatus == TicketStatus.OPEN && targetStatus == TicketStatus.IN_PROGRESS) {
            if (!isAdminOrAssignedTechnician(ticket, currentUser))
                throw new ForbiddenException("Only admins or the assigned technician can start progress");
        } else if (currentStatus == TicketStatus.RESOLVED && targetStatus == TicketStatus.CLOSED) {
            if (currentUser.getRole() != UserRole.ADMIN)
                throw new ForbiddenException("Only admins can close tickets");
            ticket.setClosedAt(LocalDateTime.now());
        } else {
            throw new InvalidStateTransitionException("Ticket", currentStatus.name(), targetStatus.name());
        }

        ticket.setStatus(targetStatus);
        Ticket saved = ticketRepository.save(ticket);

        logger.info("Ticket {} status updated to {} by user {}", ticketId, targetStatus, currentUser.getId());

        if (targetStatus == TicketStatus.IN_PROGRESS || targetStatus == TicketStatus.CLOSED) {
            notificationService.createTicketStatusChangeNotification(ticket, targetStatus);
        }

        return TicketResponse.fromEntity(saved);
    }

    public TicketResponse resolveTicket(Long ticketId, ResolveTicketRequest request, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        if (ticket.getStatus() != TicketStatus.IN_PROGRESS) {
            throw new InvalidStateTransitionException("Ticket", ticket.getStatus().name(),
                    TicketStatus.RESOLVED.name());
        }

        if (!isAdminOrAssignedTechnician(ticket, currentUser)) {
            throw new ForbiddenException("Only admins or the assigned technician can resolve this ticket");
        }

        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolutionNotes(request.getResolutionNotes());
        ticket.setResolvedAt(LocalDateTime.now());
        Ticket saved = ticketRepository.save(ticket);

        logger.info("Ticket {} resolved by {}", ticketId, currentUser.getId());

        notificationService.createTicketStatusChangeNotification(ticket, TicketStatus.RESOLVED);

        return TicketResponse.fromEntity(saved);
    }

    public TicketResponse rejectTicket(Long ticketId, RejectTicketRequest request, User adminUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new InvalidStateTransitionException("Ticket", ticket.getStatus().name(),
                    TicketStatus.REJECTED.name());
        }

        ticket.setStatus(TicketStatus.REJECTED);
        ticket.setRejectionReason(request.getRejectionReason());
        ticket.setRejectedBy(adminUser);
        Ticket saved = ticketRepository.save(ticket);

        logger.info("Ticket {} rejected by admin {}", ticketId, adminUser.getId());

        notificationService.createTicketRejectedNotification(ticket);

        return TicketResponse.fromEntity(saved);
    }

    private boolean hasTicketAccess(Ticket ticket, User user) {
        if (user.getRole() == UserRole.ADMIN)
            return true;
        if (ticket.getReporter().getId().equals(user.getId()))
            return true;
        return ticket.getAssignedTechnician() != null && ticket.getAssignedTechnician().getId().equals(user.getId());
    }

    private boolean isAdminOrAssignedTechnician(Ticket ticket, User user) {
        if (user.getRole() == UserRole.ADMIN)
            return true;
        return ticket.getAssignedTechnician() != null && ticket.getAssignedTechnician().getId().equals(user.getId());
    }
}
