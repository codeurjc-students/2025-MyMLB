package com.mlb.mlbportal.dto.mlbapi.match;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mlb.mlbportal.dto.mlbapi.team.MatchTeams;
import com.mlb.mlbportal.dto.mlbapi.team.Venue;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GameEntry(
    String gameDate,
    Status status,
    MatchTeams teams,
    Venue venue
) {}