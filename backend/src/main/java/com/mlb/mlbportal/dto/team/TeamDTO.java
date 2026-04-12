package com.mlb.mlbportal.dto.team;

import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;

/**
 * DTO that represents the stats of a team. Used to display the team stats in the standings
 */
public record TeamDTO(
    String name,
    String abbreviation,
    League league,
    Division division,
    int totalGames,
    int wins,
    int losses,
    String pct,
    Double gamesBehind,
    String lastTen
) {}