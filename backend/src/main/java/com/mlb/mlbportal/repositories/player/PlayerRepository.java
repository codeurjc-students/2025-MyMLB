package com.mlb.mlbportal.repositories.player;

import java.util.Optional;

import com.mlb.mlbportal.handler.notFound.PlayerNotFoundException;
import com.mlb.mlbportal.models.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.player.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    @EntityGraph(attributePaths = {"team"})
    Optional<Player> findByName(String name);

    default Player findByNameOrThrow(String name) {
        return this.findByName(name).orElseThrow(PlayerNotFoundException::new);
    }

    @EntityGraph(attributePaths = {"team"})
    Page<Player> findByTeamOrderByNameAsc(Team team, Pageable pageable);
}