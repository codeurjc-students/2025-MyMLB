package com.mlb.mlbportal.services.ticket;

import java.time.LocalDateTime;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mlb.mlbportal.dto.ticket.PurchaseRequest;
import com.mlb.mlbportal.dto.ticket.SeatDTO;
import com.mlb.mlbportal.dto.ticket.TicketDTO;
import com.mlb.mlbportal.handler.badRequest.SeatSelectionMismatchException;
import com.mlb.mlbportal.handler.conflict.InsufficientStockException;
import com.mlb.mlbportal.handler.notFound.TicketNotFoundException;
import com.mlb.mlbportal.mappers.ticket.TicketMapper;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.ticket.EventManager;
import com.mlb.mlbportal.models.ticket.Ticket;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.repositories.ticket.EventManagerRepository;
import com.mlb.mlbportal.repositories.ticket.EventRepository;
import com.mlb.mlbportal.repositories.ticket.SeatRepository;
import com.mlb.mlbportal.repositories.ticket.TicketRepository;
import com.mlb.mlbportal.services.EmailService;
import com.mlb.mlbportal.services.utilities.PaginationHandlerService;
import com.mlb.mlbportal.services.utilities.PdfGeneratorService;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {
    private final EventManagerRepository eventManagerRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final TicketMapper ticketMapper;
    private final PaginationHandlerService paginationHandlerService;
    private final PaymentService paymentService;
    private final EmailService emailService;
    private final PdfGeneratorService pdfGeneratorService;

    @Transactional(readOnly = true)
    public TicketDTO getTicket(Long ticketId) {
        Ticket ticket = this.ticketRepository.findById(ticketId).orElseThrow(() -> new TicketNotFoundException(ticketId));
        return this.ticketMapper.toTicketDTO(ticket);
    }

    @Transactional(readOnly = true)
    public Page<TicketDTO> getTicketsOfEvent(Long eventId, int page, int size) {
        this.eventRepository.findEventByIdOrElseThrow(eventId);
        List<Ticket> query = this.ticketRepository.findTicketByEvent(eventId);
        return this.paginationHandlerService.paginateAndMap(query, page, size, this.ticketMapper::toTicketDTO);
    }

    @Transactional
    public Page<TicketDTO> purchaseTicket(String username, PurchaseRequest request, int page, int size) {
        this.validateRequest(request);

        this.updateStocks(request);

        EventManager eventManager = this.fetchEventManagerWithLock(request.eventManagerId());
        UserEntity user = this.userRepository.findByUsernameOrThrow(username);

        this.paymentService.processPayment(request);

        List<Ticket> tickets = this.createTickets(user, request.ownerName(),eventManager, request.seats());

        this.userRepository.save(user);
        this.sendEmailWithPdf(user, tickets);

        return this.paginationHandlerService.paginateAndMap(tickets, page, size, this.ticketMapper::toTicketDTO);
    }

    /**
     * Verifies that the number of selected seats matches the requested ticket amount.
     */
    private void validateRequest(PurchaseRequest request) {
        if (request.seats().size() != request.ticketAmount()) {
            throw new SeatSelectionMismatchException();
        }
    }

    /**
     * Atomically updates event stock and marks individual seats as occupied.
     */
    private void updateStocks(PurchaseRequest request) {
        int stockUpdated = this.eventManagerRepository.updateStockAvailability(request.eventManagerId(), request.ticketAmount());
        if (stockUpdated == 0) {
            throw new InsufficientStockException();
        }
        List<Long> seatIds = request.seats().stream().map(SeatDTO::id).toList();
        int seatsMarked = this.seatRepository.markSeatAsOccupied(seatIds);
        if (seatsMarked != seatIds.size()) {
            throw new ConcurrentModificationException("One or more selected seats are no longer available");
        }
    }

    /**
     * Retrieves the EventManager using a pessimistic lock to prevent race conditions during purchase.
     */
    private EventManager fetchEventManagerWithLock(Long id) {
        return this.eventManagerRepository.findByIdWithBlock(id).orElseThrow(ConcurrentModificationException::new);
    }

    /**
     * Creates the tickets, linking them to the user, event manager, and specific seats.
     */
    private List<Ticket> createTickets(UserEntity user, String ownerName, EventManager eventManager, List<SeatDTO> seatDtos) {
        List<Ticket> tickets = new LinkedList<>();
        LocalDateTime now = LocalDateTime.now();
        for (SeatDTO seatDto : seatDtos) {
            Ticket ticket = new Ticket();
            ticket.setEventManager(eventManager);
            ticket.setOwner(user);
            ticket.setOwnerName(ownerName);
            ticket.setPurchaseDate(now);
            ticket.setSeat(this.seatRepository.getReferenceById(seatDto.id()));
            user.addTicket(ticket);
            tickets.add(ticket);
        }
        return tickets;
    }

    /**
     * Generates a PDF document and triggers an asynchronous email delivery to the purchaser.
     */
    private void sendEmailWithPdf(UserEntity user, List<Ticket> tickets) {
        try {
            byte[] pdfContent = this.pdfGeneratorService.generateTicketsPdf(tickets);
            String subject = "Game Tickets";
            String body = """
                    Hello %s, \n
                    
                    Below you may find the ticket(s) you have purchased. \n
                    
                    Thank you for your purchase and see you at the ballpark!
                    """.formatted(user.getUsername());

            this.emailService.sendTicketPurchaseEmail(
                    user.getEmail(),
                    subject,
                    body,
                    pdfContent,
                    "Tickets.pdf"
            );
        }
        catch (MessagingException e) {
            log.error("Failed to send the purchase confirmation email to the user: {}", user.getUsername());
        }
    }
}