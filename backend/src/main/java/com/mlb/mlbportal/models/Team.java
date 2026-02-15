package com.mlb.mlbportal.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
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

    private long statsApiId;

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

    @ElementCollection
    private List<Integer> championships = new LinkedList<>();

    @Enumerated(EnumType.STRING)
    private League league;

    @Enumerated(EnumType.STRING)
    private Division division;

    @OneToMany(mappedBy = "homeTeam", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Match> homeMatches = new ArrayList<>();

    @OneToMany(mappedBy = "awayTeam", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Match> awayMatches = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PositionPlayer> positionPlayers = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pitcher> pitchers = new ArrayList<>();

    @ManyToMany(mappedBy = "favTeams")
    private Set<UserEntity> favoritedByUsers = new HashSet<>();

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

        for (UserEntity user : this.favoritedByUsers) {
            user.getFavTeams().remove(this);
        }
        this.favoritedByUsers.clear();
    }

    public Team(String name, String abbreviation, int wins, int losses, String city, String info, List<Integer> championships) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.wins = wins;
        this.losses = losses;
        this.city = city;
        this.generalInfo = info;
        this.championships = championships;
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

    public void updateWins() {
        this.setWins(this.getWins() + 1);
    }

    public void updateLosses() {
        this.setLosses(this.getLosses() + 1);
    }

    public void addPositionPlayer(PositionPlayer player) {
        this.positionPlayers.add(player);
        player.setTeam(this);
    }

    public void removePositionPlayer(PositionPlayer player) {
        this.positionPlayers.remove(player);
        player.setTeam(null);
    }

    public void addPitcher(Pitcher player) {
        this.pitchers.add(player);
        player.setTeam(this);
    }

    public void removePitcher(Pitcher player) {
        this.pitchers.remove(player);
        player.setTeam(null);
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