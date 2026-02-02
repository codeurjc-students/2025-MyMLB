package com.mlb.mlbportal.dto.match;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.models.enums.MatchStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MatchDTO(
    Long id,

    @NotNull(message= "The home team is required")
    TeamSummary homeTeam,

    @NotNull(message= "The away team is required")
    TeamSummary awayTeam,

    @Min(value = 0, message = "The home team score cannot be negative")
    int homeScore,

    @Min(value = 0, message = "The away team score cannot be negative")
    int awayScore,

    @NotNull(message= "The date of the match is required")
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonFormat(pattern= "yyyy-MM-dd HH:mm")
    LocalDateTime date,

    @NotNull(message= "The status of the game is required")
    MatchStatus status,

    @NotNull(message = "The stadium is required")
    String stadiumName
) {}