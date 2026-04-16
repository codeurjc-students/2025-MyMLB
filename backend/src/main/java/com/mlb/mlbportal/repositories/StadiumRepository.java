package com.mlb.mlbportal.repositories;

import java.util.List;
import java.util.Optional;

import com.mlb.mlbportal.handler.notFound.StadiumNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.Stadium;

@Repository
public interface StadiumRepository extends JpaRepository<Stadium, Long> {
    @Override
    @EntityGraph(attributePaths = {"team"})
    Page<Stadium> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"team"})
    Optional<Stadium> findByName(String name);

    default Stadium findByNameOrThrow(String name) {
        return this.findByName(name).orElseThrow(StadiumNotFoundException::new);
    }

    @EntityGraph(attributePaths = {"team"})
    List<Stadium> findByNameContainingIgnoreCase(String input);

    @EntityGraph(attributePaths = {"team"})
    List<Stadium> findByTeamIsNull();
}