package com.mlb.mlbportal.dto.stadium;

public record StadiumInitDTO(
    String name,
    int openingDate,
    String teamName
) {}