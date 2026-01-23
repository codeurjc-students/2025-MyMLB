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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Tag(name = "AdminSupport", description = "Receives all the issues from the users and reply to each one of them")
@RestController
@RequestMapping("/api/v1/admin/support/tickets")
@RequiredArgsConstructor
public class AdminSupportController {
    private final SupportService supportService;

    @Operation(summary = "Get all open tickets", description = "Returns a list of all open tickets the admins have.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of open tickets", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SupportTicketDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<SupportTicketDTO>> getOpenTickets() {
        return ResponseEntity.ok(this.supportService.getOpenTickets());
    }

    @Operation(summary = "Get the conversation of a given ticket", description = "Returns all messages between the admin and the user of a certain ticket.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the conversation", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SupportTicketDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/{ticketId}/conversation", produces = "application/json")
    public ResponseEntity<List<SupportMessageDTO>> getConversation(@PathVariable("ticketId") UUID ticketId) {
        return ResponseEntity.ok(this.supportService.getConversation(ticketId));
    }

    @Operation(summary = "Reply to a given ticket", description = "Allows the admins to answer a ticket from the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully reply to the user", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SupportMessageDTO.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Ticket has already been closed", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/{ticketId}/reply", consumes = "application/json", produces = "application/json")
    public ResponseEntity<SupportMessageDTO> reply(@PathVariable("ticketId")UUID ticketId, @RequestBody ReplyRequest request) throws MessagingException {
        return ResponseEntity.ok(this.supportService.reply(ticketId, request));
    }

    @Operation(summary = "Close the given ticket", description = "Allows the admins to close the given ticket, ending the issue of the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully closes the ticket", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Ticket has already been closed", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/{ticketId}/close", produces = "application/json")
    public ResponseEntity<AuthResponse> closeTicket(@PathVariable("ticketId")UUID ticketId) {
        this.supportService.closeTicket(ticketId);
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Ticket Successfully Closed"));
    }
}