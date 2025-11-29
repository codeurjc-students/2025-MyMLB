package com.mlb.mlbportal.dto.player.pitcher;

import com.mlb.mlbportal.models.enums.PitcherPositions;
import com.mlb.mlbportal.models.others.PictureInfo;

public record PitcherSummaryDTO(
    String name,
    int playerNumber,
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
    int saveOpportunities,
    PictureInfo picture
) {}