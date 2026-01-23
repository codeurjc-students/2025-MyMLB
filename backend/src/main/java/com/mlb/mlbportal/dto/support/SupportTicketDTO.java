package com.mlb.mlbportal.dto.support;

import com.mlb.mlbportal.models.enums.SupportTicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SupportTicketDTO(
        UUID id,
        String subject,
        String userEmail,
        SupportTicketStatus status,
        LocalDateTime creationDate
) {}