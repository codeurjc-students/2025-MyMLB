package com.mlb.mlbportal.dto.support;

import java.time.LocalDateTime;
import java.util.UUID;

public record SupportMessageDTO(
        UUID id,
        String senderEmail,
        String body,
        boolean fromUser,
        LocalDateTime creationDate
) {}