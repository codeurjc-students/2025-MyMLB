/**
 * DTO that maps the direct response from the api for team stats and standings.
 */

package com.mlb.mlbportal.dto.mlbapi.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StandingsResponse(List<Records> records) {}