package com.mlb.mlbportal.dto.stadium;

import java.time.Year;

/**
 * General DTO for Stadium
 */
public record StadiumDTO(
    String name,
    Year openingDate
) {}