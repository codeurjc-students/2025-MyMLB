package com.mlb.mlbportal.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.Stadium;

@Repository
public interface StadiumRepository extends JpaRepository<Stadium, Long> {
    public Optional<Stadium> findByName(String name);
    public List<Stadium> findByNameContainingIgnoreCase(String input);
}