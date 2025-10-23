package com.mlb.mlbportal.services;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final Clock clock;

    public MatchService(MatchRepository matchRepo, MatchMapper mapper, Clock clock) {
        this.matchRepository = matchRepo;
        this.matchMapper = mapper;
        this.clock = clock;
    }

    public Page<MatchDTO> getMatchesOfTheDay(int page, int size) {
        LocalDate today = LocalDate.now(this.clock);
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Match> matchesOfTheDay = this.matchRepository.findByDateBetween(startOfDay, endOfDay);
        matchesOfTheDay.forEach(this::updateMatches);
        this.matchRepository.saveAll(matchesOfTheDay);

        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset(), matchesOfTheDay.size());
        int end = Math.min(start + pageable.getPageSize(), matchesOfTheDay.size());

        List<MatchDTO> paginatedDTOs = matchesOfTheDay.subList(start, end).stream()
                .map(matchMapper::toMatchDTO)
                .toList();

        return new PageImpl<>(paginatedDTOs, pageable, matchesOfTheDay.size());
    }

    private void updateMatches(Match match) {
        LocalDateTime now = LocalDateTime.now(this.clock);
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