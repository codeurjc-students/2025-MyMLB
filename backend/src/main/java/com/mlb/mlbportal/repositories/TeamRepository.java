package com.mlb.mlbportal.repositories;

import java.util.List;
import java.util.Optional;

import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByStatsApiId(Long id);

    List<Team> findByLeagueAndDivision(League league, Division division);

    Optional<Team> findByName(String name);

    default Team findByNameOrThrow(String name) {
        return this.findByName(name).orElseThrow(TeamNotFoundException::new);
    }

    Optional<Team> findByAbbreviation(String abbreviation);

    List<Team> findByNameContainingIgnoreCase(String input);

    @Query("SELECT t FROM Team t WHERE (SIZE(t.positionPlayers) + SIZE(t.pitchers)) < 24")
    List<Team> findAvailableTeams();
}