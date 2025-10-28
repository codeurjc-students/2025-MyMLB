package com.mlb.mlbportal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.player.Player;

@Repository
public interface PlayerRespository extends JpaRepository<Player, Long> {
    
}