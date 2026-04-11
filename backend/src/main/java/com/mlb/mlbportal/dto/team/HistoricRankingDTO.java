package com.mlb.mlbportal.dto.team;

import java.time.LocalDate;

public record HistoricRankingDTO(
        String teamName,
        LocalDate matchDate,
        Integer rank,
        Integer wins,
        Integer losses
) {}