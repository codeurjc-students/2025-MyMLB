package com.mlb.mlbportal.controllers.ticket;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mlb.mlbportal.dto.ticket.PurchaseRequest;
import com.mlb.mlbportal.dto.ticket.TicketDTO;
import com.mlb.mlbportal.services.ticket.TicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @GetMapping(value = "/{ticketId}", produces = "application/json")
    public ResponseEntity<TicketDTO> getTicket(@PathVariable("ticketId")Long ticketId) {
        return ResponseEntity.ok(this.ticketService.getTicket(ticketId));
    }

    @GetMapping(value = "/event/{eventId}", produces = "application/json")
    public ResponseEntity<List<TicketDTO>> getTicketsOfEvent(@PathVariable("eventId")Long eventId) {
        return ResponseEntity.ok(this.ticketService.getTicketsOfEvent(eventId));
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<TicketDTO>> purchaseTicket(Principal principal, @Valid @RequestBody PurchaseRequest request) {

        List<TicketDTO> newTickets = this.ticketService.purchaseTicket(principal.getName(), request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/{ticketId}")
                .buildAndExpand(newTickets.getFirst().id())
                .toUri();

        return ResponseEntity.created(location).body(newTickets);
    }
}