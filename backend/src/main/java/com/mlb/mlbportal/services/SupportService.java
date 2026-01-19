package com.mlb.mlbportal.services;

import com.mlb.mlbportal.dto.support.CreateTicketRequest;
import com.mlb.mlbportal.dto.support.ReplyRequest;
import com.mlb.mlbportal.dto.support.SupportMessageDTO;
import com.mlb.mlbportal.dto.support.SupportTicketDTO;
import com.mlb.mlbportal.handler.conflict.SupportTicketAlreadyIsClosedException;
import com.mlb.mlbportal.handler.notFound.SupportTicketNotFoundException;
import com.mlb.mlbportal.mappers.SupportMessageMapper;
import com.mlb.mlbportal.mappers.SupportTicketMapper;
import com.mlb.mlbportal.models.enums.SupportTicketStatus;
import com.mlb.mlbportal.models.support.SupportMessage;
import com.mlb.mlbportal.models.support.SupportTicket;
import com.mlb.mlbportal.repositories.SupportMessageRepository;
import com.mlb.mlbportal.repositories.SupportTicketRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupportService {
    private final SupportTicketRepository supportTicketRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final EmailService emailService;
    private final SupportTicketMapper supportTicketMapper;
    private final SupportMessageMapper supportMessageMapper;

    /**
     * Creates a new support ticket.
     * This ticket is created when the user contact the admins of the application looking for support.
     *
     * @param userEmail The email of the user creating the ticket.
     * @param request DTO containing the subject and body of the ticket.
     */
    @Transactional
    public void createSupportTicket(String userEmail, CreateTicketRequest request) {
        SupportTicket newTicket = SupportTicket.builder()
                .subject(request.subject())
                .userEmail(userEmail)
                .status(SupportTicketStatus.OPEN)
                .build();

        SupportMessage newMessage = SupportMessage.builder()
                .supportTicket(newTicket)
                .senderEmail(userEmail)
                .body(request.body())
                .isFromUser(true)
                .build();

        newTicket.setMessages(List.of(newMessage));
        this.supportTicketRepository.save(newTicket);
        this.supportMessageRepository.save(newMessage);
    }

    @Transactional(readOnly = true)
    public List<SupportTicketDTO> getOpenTickets() {
        return this.supportTicketMapper.toListSupportThreadDTO(this.supportTicketRepository.findAllOpenTickets());
    }

    /**
     * Retrieve the entire conversation between the user and the admin of a given ticket.
     *
     * @param ticketId The UUID of the ticket to reply to.
     * @return All the exchanged messages of the conversation.
     */
    @Transactional(readOnly = true)
    public List<SupportMessageDTO> getConversation(UUID ticketId) {
        return this.supportMessageMapper.toListSupportMessageDTO(this.supportMessageRepository.findBySupportTicketIdOrderByCreationDateAsc(ticketId));
    }

    /**
     * Adds an admin reply to a specific ticket and updates its status to 'ANSWERED'.
     * The response is sent to the user's email.
     *
     * @param ticketId The UUID of the ticket to reply to.
     * @param request DTO containing the admin email and message body.
     * @return The saved reply message as DTO.
     *
     * @throws SupportTicketNotFoundException if the ticket does not exist.
     * @throws SupportTicketAlreadyIsClosedException if the ticket is already closed.
     * @throws MessagingException if sending email fails.
     */
    @Transactional
    public SupportMessageDTO reply(UUID ticketId, ReplyRequest request) throws MessagingException {
        SupportTicket ticket = this.supportTicketRepository.findById(ticketId).orElseThrow(SupportTicketNotFoundException::new);

        if (ticket.getStatus() == SupportTicketStatus.CLOSED) {
            throw new SupportTicketAlreadyIsClosedException();
        }

        SupportMessage message = SupportMessage.builder()
                .supportTicket(ticket)
                .senderEmail(request.adminEmail())
                .body(request.body())
                .isFromUser(false)
                .build();

        this.supportMessageRepository.save(message);
        ticket.setStatus(SupportTicketStatus.ANSWERED);
        this.supportTicketRepository.save(ticket);

        this.emailService.answerToUser(ticket.getUserEmail(), "Re: " + ticket.getSubject(), request.body());
        return this.supportMessageMapper.toSupportMessageDTO(message);
    }

    /**
     * Closes a ticket.
     *
     * @param ticketId The UUID of the ticket to close.
     * @throws SupportTicketNotFoundException if the ticket does not exist.
     * @throws SupportTicketAlreadyIsClosedException if the ticket is already closed.
     */
    @Transactional
    public void closeTicket(UUID ticketId) {
        SupportTicket ticket = this.supportTicketRepository.findById(ticketId).orElseThrow(SupportTicketNotFoundException::new);
        if (ticket.getStatus() == SupportTicketStatus.CLOSED) {
            throw new SupportTicketAlreadyIsClosedException();
        }
        ticket.setStatus(SupportTicketStatus.CLOSED);
        this.supportTicketRepository.save(ticket);
    }
}