package com.smartcampus.service;

import com.smartcampus.enums.TicketStatus;
import com.smartcampus.enums.UserRole;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.FileUploadException;
import com.smartcampus.exception.ForbiddenException;
import com.smartcampus.model.Ticket;
import com.smartcampus.model.TicketAttachment;
import com.smartcampus.model.User;
import com.smartcampus.repository.TicketAttachmentRepository;
import com.smartcampus.repository.TicketRepository;
import com.smartcampus.util.FileStorageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private TicketAttachmentRepository attachmentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private FileStorageUtil fileStorageUtil;

    @InjectMocks
    private AttachmentService attachmentService;

    private User owner;
    private User admin;
    private User other;
    private Ticket ticket;
    private TicketAttachment attachment;
    private MockMultipartFile validFile;

    @BeforeEach
    void setUp() {
        owner = User.builder().role(UserRole.USER).build();
        owner.setId(1L);

        admin = User.builder().role(UserRole.ADMIN).build();
        admin.setId(2L);

        other = User.builder().role(UserRole.USER).build();
        other.setId(3L);

        ticket = Ticket.builder()
                .reporter(owner)
                .status(TicketStatus.OPEN)
                .build();
        ticket.setId(1L);

        attachment = TicketAttachment.builder()
                .ticket(ticket)
                .uploadedBy(owner)
                .originalFilename("test.png")
                .storedFilename("uuid.png")
                .build();
        attachment.setId(1L);

        validFile = new MockMultipartFile("file", "test.png", "image/png", "test image content".getBytes());
    }

    @Test
    void testUploadAttachment_ValidImage_Success() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(attachmentRepository.countByTicketId(1L)).thenReturn(2L);
        when(fileStorageUtil.storeFile(any())).thenReturn("random-uuid.png");
        when(attachmentRepository.save(any())).thenReturn(attachment);

        var response = attachmentService.uploadAttachment(1L, validFile, owner);

        assertNotNull(response);
        verify(attachmentRepository).save(any(TicketAttachment.class));
    }

    @Test
    void testUploadAttachment_NonImageFile_ThrowsException() {
        MockMultipartFile badFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(attachmentRepository.countByTicketId(1L)).thenReturn(0L);
        when(fileStorageUtil.storeFile(any())).thenThrow(new FileUploadException("Invalid file type"));

        assertThrows(FileUploadException.class, () -> attachmentService.uploadAttachment(1L, badFile, owner));
    }

    @Test
    void testUploadAttachment_MaxAttachmentsReached_BadRequestException() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(attachmentRepository.countByTicketId(1L)).thenReturn(3L);

        assertThrows(BadRequestException.class, () -> attachmentService.uploadAttachment(1L, validFile, owner));
    }

    @Test
    void testUploadAttachment_ByNonOwner_ForbiddenException() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(ForbiddenException.class, () -> attachmentService.uploadAttachment(1L, validFile, other));
    }

    @Test
    void testUploadAttachment_TicketNotOpen_BadRequestException() {
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(BadRequestException.class, () -> attachmentService.uploadAttachment(1L, validFile, owner));
    }

    @Test
    void testDeleteAttachment_ByOwnerWhenOpen_Success() {
        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));

        attachmentService.deleteAttachment(1L, owner);

        verify(fileStorageUtil).deleteFile(any());
        verify(attachmentRepository).delete(attachment);
    }

    @Test
    void testDeleteAttachment_ByAdmin_Success() {
        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));

        attachmentService.deleteAttachment(1L, admin);

        verify(fileStorageUtil).deleteFile(any());
        verify(attachmentRepository).delete(attachment);
    }

    @Test
    void testDeleteAttachment_ByNonOwnerNonAdmin_ForbiddenException() {
        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));

        assertThrows(ForbiddenException.class, () -> attachmentService.deleteAttachment(1L, other));
    }
}
