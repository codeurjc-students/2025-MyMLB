package com.mlb.mlbportal.repositories;

import java.util.List;
import java.util.Optional;

import com.mlb.mlbportal.handler.notFound.StadiumNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.Stadium;

@Repository
public interface StadiumRepository extends JpaRepository<Stadium, Long> {
    Optional<Stadium> findByName(String name);
    default Stadium findByNameOrThrow(String name) {
        return this.findByName(name).orElseThrow(StadiumNotFoundException::new);
    }
    List<Stadium> findByNameContainingIgnoreCase(String input);
    List<Stadium> findByTeamIsNull();
}