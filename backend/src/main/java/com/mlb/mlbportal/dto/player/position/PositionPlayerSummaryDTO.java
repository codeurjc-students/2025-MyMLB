package com.mlb.mlbportal.dto.player.position;

import com.mlb.mlbportal.models.enums.PlayerPositions;
import com.mlb.mlbportal.models.others.PictureInfo;

public record PositionPlayerSummaryDTO(
    String name,
    int playerNumber,
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
    double slugging,
    PictureInfo picture
) {}