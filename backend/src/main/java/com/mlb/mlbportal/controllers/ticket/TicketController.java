package com.mlb.mlbportal.controllers.ticket;

import java.net.URI;
import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mlb.mlbportal.dto.ticket.PurchaseRequest;
import com.mlb.mlbportal.dto.ticket.TicketDTO;
import com.mlb.mlbportal.services.ticket.TicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Tickets", description = "Operations for ticket purchasing, inventory management, and digital ticket retrieval")
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;

    @Operation(summary = "Get a certain ticket", description = "Return the ticket whose ID is provided")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the ticket", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDTO.class))),
            @ApiResponse(responseCode = "404", description = "Ticket Not Found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/{ticketId}", produces = "application/json")
    public ResponseEntity<TicketDTO> getTicket(@PathVariable("ticketId")Long ticketId) {
        return ResponseEntity.ok(this.ticketService.getTicket(ticketId));
    }

    @Operation(summary = "Purchase ticket(s)", description = "Allows the user to purchase one or more tickets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully purchased and created the ticket", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Ticket Not Found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Insufficient Stock", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "422", description = "Invalid Credit Card Data", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Page<TicketDTO>> purchaseTicket(Principal principal, @Valid @RequestBody PurchaseRequest request, @RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "10")int size) {

        Page<TicketDTO> newTickets = this.ticketService.purchaseTicket(principal.getName(), request, page, size);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{ticketId}")
                .buildAndExpand(newTickets.getContent().getFirst().id())
                .toUri();

        return ResponseEntity.created(location).body(newTickets);
    }
}