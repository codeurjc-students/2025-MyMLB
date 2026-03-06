/**
 * DTO that contains the stats for all MLB teams from the API.
 */

package com.mlb.mlbportal.dto.mlbapi.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Records(List<TeamRecords> teamRecords) {}