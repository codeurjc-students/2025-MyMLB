/**
 * DTO that maps the information of the stadium where the match is being held
 */

package com.mlb.mlbportal.dto.mlbapi.team;

public record Venue(
        int id,
        String name
) {}