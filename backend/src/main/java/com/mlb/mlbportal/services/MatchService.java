package com.mlb.mlbportal.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.mappers.MatchMapper;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.repositories.MatchRepository;

@Service
public class MatchService {
    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;

    public MatchService(MatchRepository matchRepo, MatchMapper mapper) {
        this.matchRepository = matchRepo;
        this.matchMapper = mapper;
    }

    public List<MatchDTO> getMatchesOfTheDay() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Match> matchesOfTheDay = this.matchRepository.findByDateBetween(startOfDay, endOfDay);

        matchesOfTheDay.forEach(this::updateMatches);
        this.matchRepository.saveAll(matchesOfTheDay);

        return this.matchMapper.toMatchDTOList(matchesOfTheDay);
    }

    private void updateMatches(Match match) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime matchTime = match.getDate();

        if (match.getStatus() == MatchStatus.Scheduled && !now.isBefore(matchTime)) {
            match.setStatus(MatchStatus.InProgress);
            return;
        }

        if (match.getStatus() == MatchStatus.InProgress && now.isAfter(matchTime.plusHours(3))) {
            match.setStatus(MatchStatus.Finished);
        }
    }

    public List<Match> getLast10Matches(Team team) {
        return this.matchRepository.findTop10ByHomeTeamOrAwayTeamOrderByDateDesc(team, team);
    }
}