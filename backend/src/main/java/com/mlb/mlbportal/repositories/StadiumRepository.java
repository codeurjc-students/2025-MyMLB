package com.mlb.mlbportal.repositories;

import java.util.List;
import java.util.Optional;

import com.mlb.mlbportal.handler.notFound.StadiumNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.Stadium;

@Repository
public interface StadiumRepository extends JpaRepository<Stadium, Long> {
    public Optional<Stadium> findByName(String name);
    public default Stadium findByNameOrThrow(String name) {
        return this.findByName(name).orElseThrow(StadiumNotFoundException::new);
    }
    public List<Stadium> findByNameContainingIgnoreCase(String input);
    public List<Stadium> findByTeamIsNull();
}