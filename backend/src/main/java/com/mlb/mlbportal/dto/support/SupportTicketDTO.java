package com.mlb.mlbportal.dto.support;

import java.time.LocalDateTime;

import com.mlb.mlbportal.models.enums.SupportTicketStatus;

public record SupportTicketDTO(
        Long id,
        String subject,
        String userEmail,
        SupportTicketStatus status,
        LocalDateTime creationDate
) {}