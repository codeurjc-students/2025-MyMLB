package com.mlb.mlbportal.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    public List<Team> findByLeagueAndDivision(League league, Division division);
}