package com.mlb.mlbportal.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.mlb.mlbportal.dto.team.FavTeamAnalyticsDTO;
import com.mlb.mlbportal.dto.team.RunsStatsDTO;
import com.mlb.mlbportal.dto.team.WinDistributionDTO;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByLeagueAndDivision(League league, Division division);

    Optional<Team> findByName(String name);

    default Team findByNameOrThrow(String name) {
        return this.findByName(name).orElseThrow(TeamNotFoundException::new);
    }

    Optional<Team> findByStatsApiId(Long id);

    default Team findByStatsApiIdOrThrow(Long id) {
        return this.findByStatsApiId(id).orElseThrow(TeamNotFoundException::new);
    }

    List<Team> findByNameContainingIgnoreCase(String input);

    @Query("SELECT t FROM Team t WHERE (SIZE(t.positionPlayers) + SIZE(t.pitchers)) < 24")
    List<Team> findAvailableTeams();

    @Query("SELECT new com.mlb.mlbportal.dto.team.FavTeamAnalyticsDTO(t.name, COUNT(u)) FROM Team  t " +
            "JOIN t.favoritedByUsers u GROUP BY t.name"
    )
    List<FavTeamAnalyticsDTO> findAllFavoriteTeamsCounter();

    @Query("SELECT t FROM Team t WHERE t.name != :teamName")
    List<Team> findRivals(@Param("teamName")String teamName);

    @Query("SELECT new com.mlb.mlbportal.dto.team.RunsStatsDTO(t.name, t.runsScored, t.runsAllowed) " +
            "FROM Team t WHERE t.name IN :teams"
    )
    List<RunsStatsDTO> findRunsStats(@Param("teams") Set<String> teams);

    @Query("SELECT new com.mlb.mlbportal.dto.team.WinDistributionDTO(t.name, t.homeGamesPlayed, t.homeGamesWins, t.roadGamesPlayed, t.roadGamesWins) FROM Team t WHERE t.name = :team")
    WinDistributionDTO findWinDistribution(@Param("team")String team);
}