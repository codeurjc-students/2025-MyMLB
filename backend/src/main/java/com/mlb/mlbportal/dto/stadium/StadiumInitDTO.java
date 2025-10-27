package com.mlb.mlbportal.dto.stadium;

import java.time.LocalDate;

public record StadiumInitDTO(
    String name,
    LocalDate openingDate,
    String teamAbbreviation
) {}