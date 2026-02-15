/**
 * DTO that maps the direct response from the API for teams.
 */

package com.mlb.mlbportal.dto.mlbapi.team;

import java.util.List;

public record TeamDetailsResponse(List<TeamDetails> teams) {}