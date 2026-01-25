package com.mlb.mlbportal.services.ticket;

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
import com.mlb.mlbportal.services.utilities.PdfGeneratorService;
import com.mysql.cj.log.Log;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final EventManagerRepository eventManagerRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final TicketMapper ticketMapper;
    private final PaymentService paymentService;
    private final EmailService emailService;
    private final PdfGeneratorService pdfGeneratorService;

    @Transactional(readOnly = true)
    public TicketDTO getTicket(Long ticketId) {
        Ticket ticket = this.ticketRepository.findById(ticketId).orElseThrow(() -> new TicketNotFoundException(ticketId));
        return this.ticketMapper.toTicketDTO(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketDTO> getTicketsOfEvent(Long eventId) {
        this.eventRepository.findEventByIdOrElseThrow(eventId);
        List<Ticket> query = this.ticketRepository.findTicketByEvent(eventId);
        return this.ticketMapper.toListTicketDTO(query);
    }

    @Transactional
    public List<TicketDTO> purchaseTicket(String username, PurchaseRequest request) {
        this.validateRequest(request);

        this.updateStocks(request);

        this.paymentService.processPayment(request);

        EventManager eventManager = this.fetchEventManagerWithLock(request.eventManagerId());
        UserEntity user = this.userRepository.findByUsernameOrThrow(username);

        List<Ticket> tickets = this.createTickets(user, request.ownerName(),eventManager, request.seats());

        this.userRepository.save(user);
        this.sendEmailWithPdf(user, tickets);

        return this.ticketMapper.toListTicketDTO(tickets);
    }

    private void validateRequest(PurchaseRequest request) {
        if (request.seats().size() != request.ticketAmount()) {
            throw new SeatSelectionMismatchException();
        }
    }

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

    private EventManager fetchEventManagerWithLock(Long id) {
        return this.eventManagerRepository.findByIdWithBlock(id).orElseThrow(ConcurrentModificationException::new);
    }

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
        catch (Exception e) {
            System.err.println("Error enviando email: " + e.getMessage()); // TODO
        }
    }
}