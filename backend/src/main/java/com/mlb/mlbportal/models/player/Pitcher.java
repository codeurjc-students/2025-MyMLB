package com.mlb.mlbportal.models.player;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.PitcherPositions;

import com.mlb.mlbportal.models.others.PictureInfo;
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

    public Pitcher(String name, int number,Team team, PitcherPositions position, int games, int wins, int losses) {
        super(name, number, team);
        this.position = position;
        this.games = games;
        this.wins = wins;
        this.losses = losses;
    }

    public Pitcher(String name, int number, Team team, PictureInfo picture, PitcherPositions position, int games, int wins) {
        super(name, number,  team, picture);
        this.position = position;
        this.games = games;
        this.wins = wins;
    }

    public Pitcher(String name, int number, Team team, PitcherPositions position) {
        super(name, number, team);
        this.position = position;
        this.games = 0;
        this.wins = 0;
        this.losses = 0;
        this.inningsPitched = 0.0;
        this.totalStrikeouts = 0;
        this.walks = 0;
        this.hitsAllowed = 0;
        this.runsAllowed = 0;
        this.saves = 0;
        this.saveOpportunities = 0;
    }
}