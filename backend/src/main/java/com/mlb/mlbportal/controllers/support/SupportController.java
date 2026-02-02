package com.mlb.mlbportal.controllers.support;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mlb.mlbportal.dto.support.CreateTicketRequest;
import com.mlb.mlbportal.security.jwt.AuthResponse;
import com.mlb.mlbportal.services.SupportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "UserSupport", description = "Allows the user to contact the admins of the applications regarding any issue they might have")
@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportController {
    private final SupportService supportService;

    @Operation(summary = "Create a Ticket", description = "Send the admins an email regarding the issue of the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created the email", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<AuthResponse> createTicket(@RequestBody CreateTicketRequest request) {
        this.supportService.createSupportTicket(request.email(), request);
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Ticket Successfully Created"));
    }
}