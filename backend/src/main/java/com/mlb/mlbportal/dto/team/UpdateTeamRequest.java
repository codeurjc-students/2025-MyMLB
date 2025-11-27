package com.mlb.mlbportal.dto.team;

import java.util.Optional;

public record UpdateTeamRequest(
        Optional<String> city,
        Optional<Integer> newChampionship,
        Optional<String> newInfo,
        Optional<String> newStadiumName
) {
}