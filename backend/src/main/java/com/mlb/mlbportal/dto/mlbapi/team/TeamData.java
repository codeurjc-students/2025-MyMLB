/**
 * DTO that maps the basic information of a team.
 */

package com.mlb.mlbportal.dto.mlbapi.team;

public record TeamData(
    int id,
    String name,
    String abbreviation
) {}