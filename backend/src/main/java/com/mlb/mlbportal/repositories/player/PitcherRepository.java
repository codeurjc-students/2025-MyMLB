package com.mlb.mlbportal.repositories.player;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.handler.notFound.PlayerNotFoundException;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.player.Pitcher;

@Repository
public interface PitcherRepository extends JpaRepository<Pitcher, Long> {
    @Override
    Page<Pitcher> findAll(Pageable pageable);

    Optional<Pitcher> findByStatsApiId(Integer id);

    Optional<Pitcher> findByName(String name);

    default Pitcher findByNameOrThrow(String name) {
        return this.findByName(name).orElseThrow(PlayerNotFoundException::new);
    }

    Page<Pitcher> findByTeamOrderByNameAsc(Team team, Pageable pageable);

    Page<Pitcher> findByNameContainingIgnoreCase(String input, Pageable pageable);
}