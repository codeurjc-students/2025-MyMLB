package com.mlb.mlbportal.dto.stadium;

import java.time.LocalDate;

public record StadiumDTO(
    String name,
    LocalDate openingDate
) {}