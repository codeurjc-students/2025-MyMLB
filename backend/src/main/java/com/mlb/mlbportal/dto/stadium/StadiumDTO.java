package com.mlb.mlbportal.dto.stadium;

import java.util.List;

/**
 * General DTO for Stadium
 */
public record StadiumDTO(
    String name,
    int openingDate,
    List<String> pictures
) {}