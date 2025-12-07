package com.mlb.mlbportal.dto.player.pitcher;

import com.mlb.mlbportal.models.enums.PitcherPositions;

import java.util.Optional;

public record EditPitcherRequest(
        Optional<String> teamName,
        Optional<Integer> playerNumber,
        Optional<PitcherPositions> position,
        Optional<Integer> games,
        Optional<Integer> wins,
        Optional<Integer> losses,
        Optional<Double> inningsPitched,
        Optional<Integer> totalStrikeouts,
        Optional<Integer> walks,
        Optional<Integer> hitsAllowed,
        Optional<Integer> runsAllowed,
        Optional<Integer> saves,
        Optional<Integer> saveOpportunities
) {}