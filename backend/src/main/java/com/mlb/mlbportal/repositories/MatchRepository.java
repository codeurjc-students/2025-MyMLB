package com.mlb.mlbportal.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.mlb.mlbportal.dto.team.WinsPerRivalDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT new com.mlb.mlbportal.dto.team.WinsPerRivalDTO(" +
            "  CASE WHEN m.homeTeam.name = :fixedTeam THEN m.awayTeam.name ELSE m.homeTeam.name END, " +
            "  COUNT(m), " + // Total Games Played
            "  SUM(CASE WHEN m.winnerTeam.name = :fixedTeam THEN 1L ELSE 0L END)" + // Wins
            ") " +
            "FROM Match m " +
            "WHERE (m.homeTeam.name = :fixedTeam OR m.awayTeam.name = :fixedTeam) " +
            "AND (m.homeTeam.name IN :rivalTeams OR m.awayTeam.name IN :rivalTeams) " +
            "AND m.status = com.mlb.mlbportal.models.enums.MatchStatus.FINISHED " +
            "GROUP BY m.homeTeam.name, m.awayTeam.name")
    List<WinsPerRivalDTO> findWinsPerRival(@Param("fixedTeam")String fixedTeam, @Param("rivalTeams") Set<String> rivalTeams);
}