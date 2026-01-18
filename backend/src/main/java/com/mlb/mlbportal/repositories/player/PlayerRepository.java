package com.mlb.mlbportal.repositories.player;

import java.util.Optional;

import com.mlb.mlbportal.handler.notFound.PlayerNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.player.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByName(String name);

    default Player findByNameOrThrow(String name) {
        return this.findByName(name).orElseThrow(PlayerNotFoundException::new);
    }
}