package com.mlb.mlbportal.dto.player;

import com.mlb.mlbportal.models.enums.PlayerPositions;

public record PositionPlayerSummaryDTO(
    String name,
    PlayerPositions position,
    int atBats,
    int walks,
    int hits,
    int doubles,
    int triples,
    int homeRuns,
    int rbis,
    double average,
    double obp,
    double ops,
    double slugging
) {}