package com.mlb.mlbportal.dto.mlbApi;

public record TeamDetails(
        int id,
        String name,
        String abbreviation,
        LeagueInfo league,
        DivisionInfo division
) {}