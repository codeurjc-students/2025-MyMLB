package com.mlb.mlbportal.dto.player.position;

import com.mlb.mlbportal.models.enums.PlayerPositions;

import java.util.Optional;

public record EditPositionPlayerRequest(
        Optional<String> teamName,
        Optional<Integer> playerNumber,
        Optional<PlayerPositions> positions,
        Optional<Integer> atBats,
        Optional<Integer> walks,
        Optional<Integer> hits,
        Optional<Integer> doubles,
        Optional<Integer> triples,
        Optional<Integer> rbis,
        Optional<Integer> homeRuns
) {}