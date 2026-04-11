package com.mlb.mlbportal.dto.team;

public record RunsStatsDTO(
        String teamName,
        Integer runsScored,
        Integer runsAllowed
) {}