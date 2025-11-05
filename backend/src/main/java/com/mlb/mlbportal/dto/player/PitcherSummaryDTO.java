package com.mlb.mlbportal.dto.player;

import com.mlb.mlbportal.models.enums.PitcherPositions;

public record PitcherSummaryDTO(
    String name,
    PitcherPositions position,
    int games,
    int wins,
    int losses,
    double era,
    int inningsPitched,
    int totalStrikeouts,
    int walks,
    int hitsAllowed,
    int runsAllowed,
    double whip,
    int saves,
    int saveOpportunities
) {}