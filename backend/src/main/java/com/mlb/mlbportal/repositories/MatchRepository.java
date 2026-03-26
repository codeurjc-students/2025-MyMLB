package com.mlb.mlbportal.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByDateBetween(LocalDateTime start, LocalDateTime end);

    Optional<Match> findByStatsApiId(Long id);

    List<Match> findTop10ByHomeTeamOrAwayTeamOrderByDateDesc(Team homeTeam, Team awayTeam);

    List<Match> findByHomeTeam(Team homeTeam);

    List<Match> findByAwayTeam(Team awayTeam);

    List<Match> findByHomeTeamOrAwayTeamAndDateBetween(Team home, Team away, LocalDateTime start, LocalDateTime end);
}