/**
 * DTO that map the stats for both pitchers and position players
 */
package com.mlb.mlbportal.dto.mlbapi.player;

public record StatData(
        // Player Stats
        Integer atBats,
        Integer hits,
        Integer baseOnBalls,
        Integer homeRuns,
        Integer rbi,
        Integer doubles,
        Integer triples,
        Double avg,
        Double obp,
        Double ops,
        Double slg,

        // Pitcher Stats
        Integer gamesPlayed,
        Integer wins,
        Integer losses,
        Integer strikeOuts,
        Integer runs,
        Integer saves,
        Integer saveOpportunities,
        Double era,
        Double whip,
        String inningsPitched
) {}