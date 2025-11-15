package com.mlb.mlbportal.dto.mlbApi;

public record GameEntry(
    String gameDate,
    Status status,
    Teams teams
) {}