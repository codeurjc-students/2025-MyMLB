package com.mlb.mlbportal.repositories.player;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.handler.notFound.PlayerNotFoundException;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.player.PositionPlayer;

@Repository
public interface PositionPlayerRepository extends JpaRepository<PositionPlayer, Long> {
    Optional<PositionPlayer> findByStatsApiId(Integer id);

    Optional<PositionPlayer> findByName(String name);

    default PositionPlayer findByNameOrThrow(String name) {
        return this.findByName(name).orElseThrow(PlayerNotFoundException::new);
    }

    List<PositionPlayer> findByTeamOrderByNameAsc(Team team);

    List<PositionPlayer> findByNameContainingIgnoreCase(String input);
}