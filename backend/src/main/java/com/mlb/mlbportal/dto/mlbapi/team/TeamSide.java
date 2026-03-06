/**
 * DTO that maps the association between the team and its score.
 */

package com.mlb.mlbportal.dto.mlbapi.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TeamSide(
    TeamData team,
    Integer score
) {}