package com.mlb.mlbportal.dto.player;

import com.mlb.mlbportal.models.enums.Positions;
import jakarta.validation.constraints.NotNull;

public record CreatePlayerRequest<T extends Positions>(
        @NotNull(message = "The player's name is required")
        String name,

        @NotNull(message = "The player's number is required")
        Integer playerNumber,

        @NotNull(message = "The player must have a team")
        String teamName,

        @NotNull(message = "The player's position is required")
        T position
) {}