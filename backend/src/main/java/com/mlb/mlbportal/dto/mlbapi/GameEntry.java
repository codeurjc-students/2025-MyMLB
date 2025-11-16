package com.mlb.mlbportal.dto.mlbapi;

public record GameEntry(
    String gameDate,
    Status status,
    Teams teams
) {}