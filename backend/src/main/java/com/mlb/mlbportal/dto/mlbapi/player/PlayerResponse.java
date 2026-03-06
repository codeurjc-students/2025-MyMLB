/**
 * DTO that map the direct response of the API. It returns the list of players
 */

package com.mlb.mlbportal.dto.mlbapi.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayerResponse(List<PlayerDetailInfo> people) {}