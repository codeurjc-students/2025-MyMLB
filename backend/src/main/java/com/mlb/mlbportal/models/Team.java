package com.mlb.mlbportal.models;

import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    private String abbreviation;

    private int totalGames;

    private int wins;

    private int losses;

    private double pct;

    private double gamesBehind;

    private int lastTen;

    private String teamLogo;

    @Enumerated(EnumType.STRING)
    private League league;

    @Enumerated(EnumType.STRING)
    private Division division;

    public Team() {}

    public Team(String name, String abbreviation, int wins, int losses, League league, Division division) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.wins = wins;
        this.losses = losses;
        this.league = league;
        this.division = division;
        this.lastTen = 0;
    }

    public Team(String name, String abbreviation, int wins, int losses, League league, Division division, String teamLogo) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.wins = wins;
        this.losses = losses;
        this.league = league;
        this.division = division;
        this.teamLogo = teamLogo;
        this.lastTen = 0;
    }
}