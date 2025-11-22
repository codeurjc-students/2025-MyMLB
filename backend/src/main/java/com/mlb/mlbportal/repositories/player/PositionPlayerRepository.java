package com.mlb.mlbportal.repositories.player;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.player.PositionPlayer;

@Repository
public interface PositionPlayerRepository extends JpaRepository<PositionPlayer, Long> {
    public Optional<PositionPlayer> findByName(String name);
    public List<PositionPlayer> findByTeamOrderByNameAsc(Team team);
    public List<PositionPlayer> findByNameContainingIgnoreCase(String input);
}