/**
 * DTO that maps the direct response from the API for teams.
 */

package com.mlb.mlbportal.dto.mlbapi.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TeamDetailsResponse(List<TeamDetails> teams) {}