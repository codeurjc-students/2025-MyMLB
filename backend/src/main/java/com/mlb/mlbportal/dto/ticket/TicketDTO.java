package com.mlb.mlbportal.dto.ticket;

import java.time.LocalDateTime;

public record TicketDTO(
        Long id,
        String awayTeamName,
        String homeTeamName,
        String stadiumName,
        double price,
        LocalDateTime matchDate,
        String sectorName,
        String seatName
) {}