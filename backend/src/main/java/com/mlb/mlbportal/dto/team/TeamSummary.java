package com.mlb.mlbportal.dto.team;

import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;

public record TeamSummary(
    String name,
    String abbreviation,
    League league,
    Division division
) {}