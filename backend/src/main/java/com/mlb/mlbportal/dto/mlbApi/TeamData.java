package com.mlb.mlbportal.dto.mlbApi;

public record TeamData(
    int id,               // importante para obtener league/division
    String name,
    String abbreviation
) {}