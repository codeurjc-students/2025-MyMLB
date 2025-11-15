package com.mlb.mlbportal.dto.mlbApi;

public record TeamSide(
    TeamData team,
    Integer score
) {}