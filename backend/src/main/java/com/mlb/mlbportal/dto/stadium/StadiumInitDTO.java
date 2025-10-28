package com.mlb.mlbportal.dto.stadium;

import java.time.Year;

public record StadiumInitDTO(
    String name,
    Year openingDate,
    String teamAbbreviation
) {}