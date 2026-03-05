/**
 * DTO that map the teams involve in a single match (home and away team)
 */

package com.mlb.mlbportal.dto.mlbapi.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MatchTeams(TeamSide home, TeamSide away) {}