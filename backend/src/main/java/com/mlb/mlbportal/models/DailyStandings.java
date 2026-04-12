package com.mlb.mlbportal.models;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "T_Daily_Standings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyStandings {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_ranking_id")
    private Team team;

    private LocalDate matchDate;
    private int rank;

    public DailyStandings(Team team, LocalDate date, int rank) {
        this.team = team;
        this.matchDate = date;
        this.rank = rank;
    }
}
