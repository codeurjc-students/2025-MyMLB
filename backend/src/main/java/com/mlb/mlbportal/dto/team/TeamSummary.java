package com.mlb.mlbportal.dto.team;

import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;

/**
 * DTO that represents the information to show for the matches
 */
public record TeamSummary(
    String name,
    String abbreviation,
    League league,
    Division division
) {}