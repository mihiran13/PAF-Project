package com.smartcampus.service;

import com.smartcampus.dto.response.AttachmentResponse;
import com.smartcampus.enums.TicketStatus;
import com.smartcampus.enums.UserRole;
import com.smartcampus.exception.BadRequestException;

import com.smartcampus.exception.ForbiddenException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.Ticket;
import com.smartcampus.model.TicketAttachment;
import com.smartcampus.model.User;
import com.smartcampus.repository.TicketAttachmentRepository;
import com.smartcampus.repository.TicketRepository;
import com.smartcampus.util.FileStorageUtil;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttachmentService {

    private final TicketAttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;
    private final FileStorageUtil fileStorageUtil;

    public AttachmentService(TicketAttachmentRepository attachmentRepository, TicketRepository ticketRepository,
            FileStorageUtil fileStorageUtil) {
        this.attachmentRepository = attachmentRepository;
        this.ticketRepository = ticketRepository;
        this.fileStorageUtil = fileStorageUtil;
    }

    public AttachmentResponse uploadAttachment(Long ticketId, MultipartFile file, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        if (!ticket.getReporter().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the ticket creator can upload files");
        }

        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new BadRequestException("Can only add attachments to tickets with OPEN status");
        }

        long currentCount = attachmentRepository.countByTicketId(ticketId);
        if (currentCount >= 3) {
            throw new BadRequestException("Maximum 3 attachments allowed per ticket");
        }

        String storedFilename = fileStorageUtil.storeFile(file);

        TicketAttachment attachment = TicketAttachment.builder()
                .ticket(ticket)
                .originalFilename(file.getOriginalFilename())
                .storedFilename(storedFilename)
                .filePath("tickets/" + storedFilename)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .uploadedBy(currentUser)
                .build();

        TicketAttachment saved = attachmentRepository.save(attachment);
        return AttachmentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsByTicketId(Long ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        if (!hasTicketAccess(ticket, currentUser)) {
            throw new ForbiddenException("You do not have access to view this ticket's attachments");
        }

        return attachmentRepository.findByTicketId(ticketId).stream()
                .map(AttachmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Resource downloadAttachment(Long attachmentId, User currentUser) {
        TicketAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketAttachment", "id", attachmentId));

        Ticket ticket = attachment.getTicket();
        if (!hasTicketAccess(ticket, currentUser)) {
            throw new ForbiddenException("You do not have access to download this attachment");
        }

        return fileStorageUtil.loadFileAsResource(attachment.getStoredFilename());
    }

    public void deleteAttachment(Long attachmentId, User currentUser) {
        TicketAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketAttachment", "id", attachmentId));

        Ticket ticket = attachment.getTicket();

        boolean ownerActive = ticket.getReporter().getId().equals(currentUser.getId())
                && ticket.getStatus() == TicketStatus.OPEN;
        boolean admin = currentUser.getRole() == UserRole.ADMIN;

        if (!ownerActive && !admin) {
            throw new ForbiddenException("You cannot delete this attachment");
        }

        fileStorageUtil.deleteFile(attachment.getStoredFilename());
        attachmentRepository.delete(attachment);
    }

    private boolean hasTicketAccess(Ticket ticket, User user) {
        if (user.getRole() == UserRole.ADMIN)
            return true;
        if (ticket.getReporter().getId().equals(user.getId()))
            return true;
        return ticket.getAssignedTechnician() != null && ticket.getAssignedTechnician().getId().equals(user.getId());
    }
}
