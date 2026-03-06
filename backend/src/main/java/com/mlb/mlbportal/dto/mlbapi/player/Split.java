/**
 * DTO that map the breakdown of a certain player in a season. If the player played for more than 1 team during the season, he will have more than 1 split.
 */
package com.mlb.mlbportal.dto.mlbapi.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Split(StatData stat) {}