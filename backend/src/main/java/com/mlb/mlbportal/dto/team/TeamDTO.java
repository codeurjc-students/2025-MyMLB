package com.mlb.mlbportal.dto.team;

import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TeamDTO(
    @NotBlank(message = "The name of the team is required")
    String name,

    @NotBlank(message = "The abbreviation of the team is required")
    String abbreviation,

    @NotNull(message = "The team League is required")
    League league,

    @NotNull(message = "The team division is required")
    Division division,

    @Min(value = 0, message = "The amount of games cannot be negative")
    @Max(value = 162, message = "The amount of games cannot be greater than 162")
    int totalGames,

    @Min(value = 0, message = "Wins cannot be negative")
    @Max(value = 162, message = "The amount of wins cannot be greater than 162")
    int wins,

    @Min(value = 0, message = "Losses cannot be negative")
    @Max(value = 162, message = "The amount of losses cannot be greater than 162")
    int losses,

    @DecimalMin(value = "0.0", inclusive = true, message = "Winning percentage cannot be negative")
    @DecimalMax(value = "1.0", inclusive = true, message = "Winning percentage cannot exceed 1.0")
    double pct,

    @DecimalMin(value = "0.0", inclusive = true, message = "Games Behind cannot be negative")
    double gamesBehind,

    @NotBlank(message = "The record of the team in the last 10 games is required")
    String lastTen
) {}