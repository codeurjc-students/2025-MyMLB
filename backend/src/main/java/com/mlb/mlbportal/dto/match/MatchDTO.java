package com.mlb.mlbportal.dto.match;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.models.enums.MatchStatus;

public record MatchDTO(
    Long id,

    TeamSummary homeTeam,

    TeamSummary awayTeam,

    int homeScore,

    int awayScore,

    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonFormat(pattern= "yyyy-MM-dd HH:mm", timezone = "Europe/Madrid")
    LocalDateTime date,

    MatchStatus status,

    String stadiumName
) {}