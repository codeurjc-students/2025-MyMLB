package com.mlb.mlbportal.dto.mlbapi.match;

import com.mlb.mlbportal.dto.mlbapi.team.MatchTeams;
import com.mlb.mlbportal.dto.mlbapi.team.Venue;

public record GameEntry(
    String gameDate,
    Status status,
    MatchTeams teams,
    Venue venue
) {}