package com.mlb.mlbportal.dto.team;

public record WinsPerRivalDTO(
        String rivalTeamName,
        Long gamesPlayed,
        Long wins
) {}