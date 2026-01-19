package com.mlb.mlbportal.controllers.support;

import com.mlb.mlbportal.dto.support.CreateTicketRequest;
import com.mlb.mlbportal.security.jwt.AuthResponse;
import com.mlb.mlbportal.services.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportController {
    private final SupportService supportService;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<AuthResponse> createTicket(@RequestBody CreateTicketRequest request) {
        this.supportService.createSupportTicket(request.email(), request);
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Ticket Successfully Created"));
    }
}