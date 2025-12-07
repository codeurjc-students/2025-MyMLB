package com.mlb.mlbportal.dto.player.pitcher;

import com.mlb.mlbportal.models.enums.PitcherPositions;
import jakarta.validation.constraints.NotNull;

public record CreatePitcherRequest(
        @NotNull(message = "The player's name is required")
        String name,

        @NotNull(message = "The player's number is required")
        Integer playerNumber,

        @NotNull(message = "The player must have a team")
        String teamName,

        @NotNull(message = "The player's position is required")
        PitcherPositions position
) {}