package com.mlb.mlbportal.dto.ticket;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record EditEventRequest(
        @NotNull(message = "EventID is required")
        Long eventId,

        @Size(min = 1, message = "The sectors are required")
        @NotNull(message = "The sectors are required")
        List<Long> sectorIds,

        @Size(min = 1, message = "The sectors are required")
        @NotNull(message = "The sectors are required")
        List<Double> prices
) {}