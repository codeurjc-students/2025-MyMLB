package com.mlb.mlbportal.repositories.player;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.handler.notFound.PlayerNotFoundException;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.player.Pitcher;

@Repository
public interface PitcherRepository extends JpaRepository<Pitcher, Long> {
    Optional<Pitcher> findByStatsApiId(Integer id);

    Optional<Pitcher> findByName(String name);

    default Pitcher findByNameOrThrow(String name) {
        return this.findByName(name).orElseThrow(PlayerNotFoundException::new);
    }

    List<Pitcher> findByTeamOrderByNameAsc(Team team);

    List<Pitcher> findByNameContainingIgnoreCase(String input);
}