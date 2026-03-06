/**
 * DTO that map all the stats of a certain player for the current season.
 */

package com.mlb.mlbportal.dto.mlbapi.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Stats(List<Split> splits) {}