package com.mlb.mlbportal.dto.mlbapi;

public record TeamDetails(
        int id,
        String name,
        String abbreviation,
        LeagueInfo league,
        DivisionInfo division
) {}