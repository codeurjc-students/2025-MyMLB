package com.mlb.mlbportal.services;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.naming.ServiceUnavailableException;

import com.mlb.mlbportal.dto.mlbapi.DateEntry;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.services.team.TeamService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.dto.mlbapi.GameEntry;
import com.mlb.mlbportal.dto.mlbapi.ScheduleResponse;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.TeamRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MatchImportService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final TeamImportService teamLookupService;
    private final TeamService teamService;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;
    private final Clock clock;

    @CircuitBreaker(name = "mlbApi", fallbackMethod = "fallbackMatches")
    @Retry(name = "mlbApi")
    public List<MatchDTO> getOfficialMatches(LocalDate start, LocalDate end) {
        String url = "https://statsapi.mlb.com/api/v1/schedule?sportId=1" +
                     "&startDate=" + start + "&endDate=" + end;

        ScheduleResponse response = this.restTemplate.getForObject(url, ScheduleResponse.class);

        if (response == null || response.dates() == null) {
            return List.of();
        }

        return response.dates().stream()
                .flatMap(d -> d.games().stream())
                .map(this::toMatchDTO)
                .filter(this::isMlbTeamMatch)
                .filter(dto -> this.stadiumRepository.findByName(dto.stadiumName()).isPresent())
                .map(this::saveMatch)
                .toList();
    }

    @Async
    @Transactional
    @CircuitBreaker(name = "verifyMatchStatus", fallbackMethod = "fallbackMatches")
    @Retry(name = "verifyMatchStatus")
    public void verifyMatchStatus() {
        LocalDate today = LocalDate.now(this.clock);
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        String url = "https://statsapi.mlb.com/api/v1/schedule?sportId=1" +
                "&startDate=" + startOfDay + "&endDate=" + endOfDay;

        ScheduleResponse response = this.restTemplate.getForObject(url, ScheduleResponse.class);

        if (response == null || response.dates() == null) {
            throw new NoSuchElementException("No games scheduled for today");
        }

        for (DateEntry dateEntry : response.dates()) {
            for (GameEntry gameEntry : dateEntry.games()) {
                MatchDTO apiMatch = this.toMatchDTO(gameEntry);
                this.matchRepository.findById(apiMatch.id()).ifPresent(match -> {
                    MatchStatus oldStatus = match.getStatus();
                    MatchStatus newStatus = apiMatch.status();
                    match.setHomeScore(apiMatch.homeScore());
                    match.setAwayScore(apiMatch.awayScore());

                    if (!oldStatus.equals(newStatus)) {
                        match.setStatus(newStatus);
                        this.matchRepository.save(match);
                        if (!oldStatus.equals(MatchStatus.FINISHED) && newStatus.equals(MatchStatus.FINISHED)) {
                            this.updateStatus(match);
                        }
                    }
                });
            }
        }
    }

    private void updateStatus(Match match) {
        Team awayTeam = match.getAwayTeam();
        Team homeTeam = match.getHomeTeam();
        if (this.isHomeTeamWinner(match)) {
            homeTeam.updateWins();
            awayTeam.updateLosses();
        }
        else {
            awayTeam.updateWins();
            homeTeam.updateLosses();
        }
        this.teamRepository.save(awayTeam);
        this.teamRepository.save(homeTeam);
        this.teamService.updateRanking(awayTeam, homeTeam);
    }

    private boolean isHomeTeamWinner(Match match) {
        int awayScore = match.getAwayScore();
        int homeScore = match.getHomeScore();
        return homeScore > awayScore;
    }

    private MatchDTO saveMatch(MatchDTO dto) {
        Team homeEntity = this.teamRepository.findByName(dto.homeTeam().name()).orElseThrow(TeamNotFoundException::new);
        Team awayEntity = this.teamRepository.findByName(dto.awayTeam().name()).orElseThrow(TeamNotFoundException::new);

        Stadium stadium = this.stadiumRepository.findByNameOrThrow(dto.stadiumName());

        Match match = new Match(awayEntity, homeEntity, dto.awayScore(), dto.homeScore(), dto.date(), dto.status());
        match.setStadium(stadium);

        Match savedMatch = this.matchRepository.save(match);
        return new MatchDTO(
                savedMatch.getId(),
                dto.homeTeam(),
                dto.awayTeam(),
                dto.homeScore(),
                dto.awayScore(),
                dto.date(),
                dto.status(),
                dto.stadiumName()
        );
    }

    @SuppressWarnings("unused")
    private List<MatchDTO> fallbackMatches(LocalDate start, LocalDate end, Throwable t) throws ServiceUnavailableException {
        List<Match> cachedMatches = this.matchRepository.findByDateBetween(start.atStartOfDay(), end.atTime(23,59));

        if (!cachedMatches.isEmpty()) {
            return cachedMatches.stream()
                    .map(m -> new MatchDTO(
                            m.getId(),
                            new TeamSummary(
                                m.getHomeTeam().getName(),
                                m.getHomeTeam().getAbbreviation(),
                                m.getHomeTeam().getLeague(),
                                m.getHomeTeam().getDivision()
                            ),
                            new TeamSummary(
                                m.getAwayTeam().getName(),
                                m.getAwayTeam().getAbbreviation(),
                                m.getAwayTeam().getLeague(),
                                m.getAwayTeam().getDivision()
                            ),
                            m.getHomeScore(),
                            m.getAwayScore(),
                            m.getDate(),
                            m.getStatus(),
                            m.getStadium().getName()
                    ))
                    .toList();
        }
        throw new ServiceUnavailableException("MLB API not available and without cached data");
    }

    private boolean isMlbTeamMatch(MatchDTO dto) {
        boolean homeExists = this.teamRepository.findByName(dto.homeTeam().name()).isPresent();
        boolean awayExists = this.teamRepository.findByName(dto.awayTeam().name()).isPresent();
        return homeExists && awayExists;
    }

    private MatchDTO toMatchDTO(GameEntry game) {
        int homeId = game.teams().home().team().id();
        int awayId = game.teams().away().team().id();
        TeamSummary home = this.teamLookupService.getTeamSummary(homeId);
        TeamSummary away = this.teamLookupService.getTeamSummary(awayId);
        LocalDateTime date = LocalDateTime.parse(game.gameDate().replace("Z", ""));
        MatchStatus status = this.convertStatus(game.status().detailedState());
        String stadiumName = game.venue().name();
        if (stadiumName.equals("Rate Field")) {
            stadiumName = "Guaranteed Rate Field";
        }
        return new MatchDTO(null, home, away, Objects.requireNonNullElse(game.teams().home().score(), 0),
                Objects.requireNonNullElse(game.teams().away().score(), 0), date, status, stadiumName);
    }

    private MatchStatus convertStatus(String apiStatus) {
        return switch (apiStatus) {
            case "In Progress" -> MatchStatus.IN_PROGRESS;
            case "Final" -> MatchStatus.FINISHED;
            default -> MatchStatus.SCHEDULED;
        };
    }
}