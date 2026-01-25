package com.mlb.mlbportal.dto.ticket;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record EventCreateRequest(
        @NotNull(message = "MatchID is required")
        Long matchId,

        @NotNull(message = "Price of the ticket is required")
        List<Double> prices,

        @Size(min = 1, message = "The sectors are required")
        @NotNull(message = "The sectors are required")
        List<SectorCreateRequest> sectors
) {}