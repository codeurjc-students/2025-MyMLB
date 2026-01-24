package com.mlb.mlbportal.services.ticket;

import com.mlb.mlbportal.dto.ticket.PurchaseRequest;
import com.mlb.mlbportal.dto.ticket.TicketDTO;
import com.mlb.mlbportal.mappers.ticket.TicketMapper;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.ticket.EventManager;
import com.mlb.mlbportal.models.ticket.Ticket;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.repositories.ticket.EventManagerRepository;
import com.mlb.mlbportal.repositories.ticket.EventRepository;
import com.mlb.mlbportal.repositories.ticket.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketService {
    private final EventManagerRepository eventManagerRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketMapper ticketMapper;
    private final PaymentService paymentService;

    @Transactional(readOnly = true)
    public List<TicketDTO> getTicketsOfEvent(Long eventId) {
        this.eventRepository.findEventByIdOrElseThrow(eventId);
        List<Ticket> query = this.ticketRepository.findTicketByEvent(eventId);
        return this.ticketMapper.toListTicketDTO(query);
    }

    @Transactional
    public List<TicketDTO> purchaseTicket(String username, PurchaseRequest request) {
        int rowsUpdated = this.eventManagerRepository.updateStockAvailability(request.eventManagerId(), request.ticketAmount());
        if (rowsUpdated == 0) {
            throw new IllegalArgumentException();
        }
        this.paymentService.processPayment(request);

        EventManager eventManager = this.eventManagerRepository.findByIdWithBlock(request.eventManagerId()).orElseThrow(ConcurrentModificationException::new);
        UserEntity user = this.userRepository.findByUsernameOrThrow(username);
        List<Ticket> tickets = new LinkedList<>();

        for (int i = 0; i < request.ticketAmount(); i++) {
            Ticket ticket = new Ticket();
            ticket.setEventManager(eventManager);
            ticket.setOwner(user);
            ticket.setPurchaseDate(LocalDateTime.now());
            user.addTicket(ticket);
            tickets.add(ticket);
        }
        this.userRepository.save(user);
        this.ticketRepository.saveAll(tickets);
        return this.ticketMapper.toListTicketDTO(tickets);
    }
}