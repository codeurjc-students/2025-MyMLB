package com.mlb.mlbportal.controllers.support;

import com.mlb.mlbportal.dto.support.ReplyRequest;
import com.mlb.mlbportal.dto.support.SupportMessageDTO;
import com.mlb.mlbportal.dto.support.SupportTicketDTO;
import com.mlb.mlbportal.security.jwt.AuthResponse;
import com.mlb.mlbportal.services.SupportService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/support/tickets")
@RequiredArgsConstructor
public class AdminSupportController {
    private final SupportService supportService;

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<SupportTicketDTO>> getOpenTickets() {
        return ResponseEntity.ok(this.supportService.getOpenTickets());
    }

    @GetMapping(value = "/{ticketId}/conversation", produces = "application/json")
    public ResponseEntity<List<SupportMessageDTO>> getConversation(@PathVariable("ticketId") UUID ticketId) {
        return ResponseEntity.ok(this.supportService.getConversation(ticketId));
    }

    @PostMapping(value = "/{ticketId}/reply", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SupportMessageDTO> reply(@PathVariable("ticketId")UUID ticketId, @RequestBody ReplyRequest request) throws MessagingException {
        return ResponseEntity.ok(this.supportService.reply(ticketId, request));
    }

    @PostMapping(value = "/{ticketId}/close", produces = "application/json")
    public ResponseEntity<AuthResponse> closeTicket(@PathVariable("ticketId")UUID ticketId) {
        this.supportService.closeTicket(ticketId);
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Ticket Successfully Closed"));
    }
}