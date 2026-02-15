/**
 * This DTO map additional data of the player. Comparing to PlayerData, this DTO adds the player's number, position, and his stats for the current season
 * */
package com.mlb.mlbportal.dto.mlbapi.player;

import java.util.List;

public record PlayerDetailInfo(
        int id,
        String fullName,
        String primaryNumber,
        PositionData primaryPosition,
        List<Stats> stats
) {}