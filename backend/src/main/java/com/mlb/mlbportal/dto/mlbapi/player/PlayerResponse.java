/**
 * DTO that map the direct response of the API. It returns the list of players
 */

package com.mlb.mlbportal.dto.mlbapi.player;

import java.util.List;

public record PlayerResponse(List<PlayerDetailInfo> people) {}