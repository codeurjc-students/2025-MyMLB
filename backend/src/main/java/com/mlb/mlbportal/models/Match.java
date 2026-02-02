package com.mlb.mlbportal.models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mlb.mlbportal.models.enums.MatchStatus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "T_Matches")
public class Match {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;

    @ManyToOne
    private Team homeTeam;

    @ManyToOne
    private Team awayTeam;

    private int homeScore;

    private int awayScore;

    @JsonFormat(pattern = "MM/dd/yyyy HH:mm")
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    public Match(Team awayTeam, Team homeTeam, int awayScore, int homeScore, LocalDateTime date, MatchStatus status) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.date = date;
        this.status = status;
    }
}