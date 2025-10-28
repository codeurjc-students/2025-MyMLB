package com.mlb.mlbportal.models.player;

import java.util.Map;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.PitcherPositions;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pitcher extends Player {
    @Enumerated(EnumType.STRING)
    private PitcherPositions position;

    private int games;
    private double era;
    private int wins;
    private int losses;
    private int inningsPitched;
    private int totalStrikeouts;
    private int totalBaseOnBalls;

    public Pitcher(String name, Team team, PitcherPositions position, int games, double era, int wins, int losses, int inningsPitched, int totalStrikeouts, int totalBaseOnBalls) {
        super(name, team);
        this.position = position;
        this.games = games;
        this.era = era;
        this.wins = wins;
        this.losses = losses;
        this.inningsPitched = inningsPitched;
        this.totalStrikeouts = totalStrikeouts;
        this.totalBaseOnBalls = totalBaseOnBalls;
    }

    public Pitcher(String name, Team team, String picture, PitcherPositions position, int games, double era, int wins, int losses, int inningsPitched, int totalStrikeouts, int totalBaseOnBalls) {
        super(name, team, picture);
        this.position = position;
        this.games = games;
        this.era = era;
        this.wins = wins;
        this.losses = losses;
        this.inningsPitched = inningsPitched;
        this.totalStrikeouts = totalStrikeouts;
        this.totalBaseOnBalls = totalBaseOnBalls;
    }
    
    @Override
    public Map<String, Object> getStats() {
       return Map.of(
            "Position", this.position,
            "G", this.games,
            "W", this.wins,
            "L", this.losses,
            "IP", this.inningsPitched,
            "ERA", this.era,
            "SO", this.totalStrikeouts,
            "BB", this.totalBaseOnBalls
        );
    }
}