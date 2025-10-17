package com.mlb.mlbportal.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.repositories.MatchRepository;

@Service
public class MatchService {
    private final MatchRepository matchRepository;

    public MatchService(MatchRepository matchRepo) {
        this.matchRepository = matchRepo;
    }

    public List<Match> getLast10Matches(Team team) {
        return this.matchRepository.findTop10ByHomeTeamOrAwayTeamOrderByDateDesc(team, team);
    }
}