package com.mlb.mlbportal.dto.team;

public record TeamInitDTO(
    String name,
    String abbreviation,
    int wins,
    int losses,
    String league,
    String division
) {}