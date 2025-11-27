package com.mlb.mlbportal.dto.stadium;

import jakarta.validation.constraints.NotNull;

public record CreateStadiumRequest(
        @NotNull(message = "The name of the new stadium is required")
        String name,

        @NotNull(message = "The opening date of the new stadium is required")
        Integer openingDate
) {}