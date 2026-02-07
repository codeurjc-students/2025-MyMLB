package com.mlb.mlbportal.dto.support;

import java.time.LocalDateTime;

public record SupportMessageDTO(
        Long id,
        String senderEmail,
        String body,
        boolean fromUser,
        LocalDateTime creationDate
) {}