package com.mlb.mlbportal.models.player;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.PlayerPositions;

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

    public PositionPlayer(String name, int number, Team team, PlayerPositions position, int atBats, int walks, int hits, int doubles) {
        super(name, number, team);
        this.position = position;
        this.atBats = atBats;
        this.hits = hits;
        this.walks = walks;
        this.doubles = doubles;
    }

    public PositionPlayer(String name, int number, Team team, PictureInfo picture, PlayerPositions position, int atBats, int walks, int hits) {
        super(name, number, team, picture);
        this.position = position;
        this.atBats = atBats;
        this.walks = walks;
        this.hits = hits;
    }

    public PositionPlayer(String name, int number, Team team, PlayerPositions position) {
        super(name, number, team);
        this.position = position;
        this.atBats = 0;
        this.walks = 0;
        this.hits = 0;
        this.doubles = 0;
        this.triples = 0;
        this.homeRuns = 0;
        this.rbis = 0;
    }
}