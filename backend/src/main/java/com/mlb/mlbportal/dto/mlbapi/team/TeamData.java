/**
 * DTO that maps the basic information of a team.
 */

package com.mlb.mlbportal.dto.mlbapi.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TeamData(
    int id,
    String name,
    String abbreviation
) {}