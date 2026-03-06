/**
 * DTO that maps the split stats of a team from the API (Last 10 Games).
 */

package com.mlb.mlbportal.dto.mlbapi.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SplitRecords(
        Integer wins,
        Integer losses,
        String type
) {}