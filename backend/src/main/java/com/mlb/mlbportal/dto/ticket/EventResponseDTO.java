package com.mlb.mlbportal.dto.ticket;

import java.time.LocalDateTime;
import java.util.List;

public record EventResponseDTO(
        Long id,
        String awayTeamName,
        String homeTeamName,
        String stadiumName,
        LocalDateTime date,
        List<EventManagerDTO> sectors
) {}