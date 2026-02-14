/**
 * DTO that maps the association between the team and its score.
 */

package com.mlb.mlbportal.dto.mlbapi.team;

public record TeamSide(
    TeamData team,
    Integer score
) {}