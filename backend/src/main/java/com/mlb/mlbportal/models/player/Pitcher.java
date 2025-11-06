package com.mlb.mlbportal.models.player;

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
    private double inningsPitched;
    private int totalStrikeouts;
    private int walks;
    private int hitsAllowed;
    private int runsAllowed;
    private int saves;
    private int saveOpportunities;
    private double whip;

    public Pitcher(String name, Team team, PitcherPositions position, int games, int wins, int losses, double inningsPitched) {
        super(name, team);
        this.position = position;
        this.games = games;
        this.wins = wins;
        this.losses = losses;
        this.inningsPitched = inningsPitched;
    }

    public Pitcher(String name, Team team, String picture, PitcherPositions position, int games, int wins, int losses) {
        super(name, team, picture);
        this.position = position;
        this.games = games;
        this.wins = wins;
        this.losses = losses;
    }
}