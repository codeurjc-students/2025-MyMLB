package com.mlb.mlbportal.dto.player;

import com.mlb.mlbportal.models.enums.PlayerPositions;

public record PositionPlayerDTO(
    String name,
    String teamName,
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
) implements PlayerDTO {

    @Override
    public String type() {
        return "PositionPlayer";
    }
}