package com.mlb.mlbportal.models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreRemove;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
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

    private String lastTen;

    private String teamLogo;

    private String city;

    @Lob
    private String generalInfo;
    
    private List<Integer> championships = new LinkedList<>();

    @Enumerated(EnumType.STRING)
    private League league;

    @Enumerated(EnumType.STRING)
    private Division division;

    @OneToMany(mappedBy = "homeTeam", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<Match> homeMatches = new ArrayList<>();

    @OneToMany(mappedBy = "awayTeam", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<Match> awayMatches = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PositionPlayer> positionPlayers = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Pitcher> pitchers = new ArrayList<>();

    @PreRemove
    public void preRemove() {
        if (stadium != null) {
            stadium.setTeam(null);
        }

        for (PositionPlayer player: this.positionPlayers) {
            player.setTeam(null);
        }

        for (Pitcher pitcher : this.pitchers) {
            pitcher.setTeam(null);
        }
    }

    public Team(String name, String abbreviation, int wins, int losses, String city, String info, List<Integer> championships, League league, Division division) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.wins = wins;
        this.losses = losses;
        this.city = city;
        this.generalInfo = info;
        this.championships = championships;
        this.league = league;
        this.division = division;
        this.lastTen = "0-0";
    }

    public Team(String name, String abbreviation, int wins, int losses, League league, Division division) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.wins = wins;
        this.losses = losses;
        this.league = league;
        this.division = division;
        this.lastTen = "0-0";
    }

    public Team(String name, String abbreviation, int wins, int losses, League league, Division division, String teamLogo) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.wins = wins;
        this.losses = losses;
        this.league = league;
        this.division = division;
        this.teamLogo = teamLogo;
        this.lastTen = "0-0";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Team other = (Team) obj;
        return name != null && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}