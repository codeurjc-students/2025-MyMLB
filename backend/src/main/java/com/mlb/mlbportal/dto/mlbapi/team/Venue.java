/**
 * DTO that maps the information of the stadium where the match is being held
 */

package com.mlb.mlbportal.dto.mlbapi.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Venue(
        int id,
        String name
) {}