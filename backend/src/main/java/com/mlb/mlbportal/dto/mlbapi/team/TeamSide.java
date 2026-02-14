package com.mlb.mlbportal.dto.mlbapi;

public record TeamSide(
    TeamData team,
    Integer score
) {}