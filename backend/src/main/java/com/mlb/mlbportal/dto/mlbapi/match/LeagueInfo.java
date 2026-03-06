/**
 * DTO that maps the league´s data (only the name at the moment).
 */

package com.mlb.mlbportal.dto.mlbapi.match;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LeagueInfo(String name) {}