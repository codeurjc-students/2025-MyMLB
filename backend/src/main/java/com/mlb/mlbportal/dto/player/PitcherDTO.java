package com.mlb.mlbportal.dto.player;

import com.mlb.mlbportal.models.enums.PitcherPositions;

public record PitcherDTO(
    String name,
    String teamName,
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
) implements PlayerDTO {

    @Override
    public String type() {
        return "Pitcher";
    }
}