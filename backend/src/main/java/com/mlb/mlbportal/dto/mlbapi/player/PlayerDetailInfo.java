/**
 * This DTO map additional data of the player. Comparing to PlayerData, this DTO adds the player's number, position, and his stats for the current season
 * */
package com.mlb.mlbportal.dto.mlbapi.player;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayerDetailInfo(
        int id,
        String fullName,
        String primaryNumber,
        PositionData primaryPosition,
        List<Stats> stats
) {}