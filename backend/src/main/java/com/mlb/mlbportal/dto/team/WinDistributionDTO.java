package com.mlb.mlbportal.dto.team;

public record WinDistributionDTO(
        String teamName,
        Long homeGames,
        Long homeWins,
        Long roadGames,
        Long roadWins
) {
    public Double getHomeWinPct() {
        if (this.homeGames == null || this.homeGames == 0) {
            return 0.0;
        }
        double pct = (double) this.homeWins / this.homeGames;
        return Math.round(pct * 1000.0) / 1000.0;
    }

    public Double getRoadWinPct() {
        if (this.roadGames == null || this.roadGames == 0) {
            return 0.0;
        }
        double pct = (double) this.roadWins / this.roadWins;
        return Math.round(pct * 1000.0) / 1000.0;
    }
}