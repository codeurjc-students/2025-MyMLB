package com.mlb.mlbportal.repositories.player;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.player.Pitcher;

@Repository
public interface PitcherRepository extends JpaRepository<Pitcher, Long> {
    public Optional<Pitcher> findByName(String name);
}