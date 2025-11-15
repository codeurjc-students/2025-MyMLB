package com.mlb.mlbportal.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    public List<Match> findByDateBetween(LocalDateTime start, LocalDateTime end);
    public List<Match> findTop10ByHomeTeamOrAwayTeamOrderByDateDesc(Team homeTeam, Team awayTeam);
    public boolean existsByHomeTeamNameAndAwayTeamNameAndDate(String homeTeamName, String awayTeamName, LocalDateTime date);
    public List<Match> findByHomeTeam(Team homeTeam);
    public List<Match> findByAwayTeam(Team awayTeam);
}