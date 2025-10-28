package com.mlb.mlbportal.models.player;

import java.util.Map;

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
    private int homeRuns;
    private int rbis;
    private double average;

    public PositionPlayer(String name, Team team, PlayerPositions position, int atBats, int hits, int homeRuns, int rbis, double average) {
        super(name, team);
        this.position = position;
        this.atBats = atBats;
        this.hits = hits;
        this.homeRuns = homeRuns;
        this.rbis = rbis;
        this.average = average;
    }

    public PositionPlayer(String name, Team team, String picture, PlayerPositions position, int atBats, int hits, int homeRuns, int rbis, double average) {
        super(name, team, picture);
        this.position = position;
        this.atBats = atBats;
        this.hits = hits;
        this.homeRuns = homeRuns;
        this.rbis = rbis;
        this.average = average;
    }

    @Override
    public Map<String, Object> getStats() {
        return Map.of(
            "Position", this.position,
            "AVG", this.average,
            "AB", this.atBats,
            "H", this.hits,
            "HR", this.homeRuns,
            "RBIs", this.rbis 
        );
    }
}