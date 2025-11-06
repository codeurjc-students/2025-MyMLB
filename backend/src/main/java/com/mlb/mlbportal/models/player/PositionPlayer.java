package com.mlb.mlbportal.models.player;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.PlayerPositions;

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
public class PositionPlayer extends Player {
    @Enumerated(EnumType.STRING)
    private PlayerPositions position;

    private int atBats;
    private int hits;
    private int walks;
    private int homeRuns;
    private int rbis;
    private double average;
    private double obp;
    private double ops;
    private int doubles;
    private int triples;
    private double slugging;

    public PositionPlayer(String name, Team team, PlayerPositions position, int atBats, int walks, int hits, int doubles) {
        super(name, team);
        this.position = position;
        this.atBats = atBats;
        this.hits = hits;
        this.walks = walks;
        this.doubles = doubles;
    }

    public PositionPlayer(String name, Team team, String picture, PlayerPositions position, int atBats, int walks, int hits) {
        super(name, team, picture);
        this.position = position;
        this.atBats = atBats;
        this.walks = walks;
        this.hits = hits;
    }
}