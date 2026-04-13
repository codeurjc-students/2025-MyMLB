package com.mlb.mlbportal.services.mlbAPI;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import javax.naming.ServiceUnavailableException;

import com.mlb.mlbportal.services.utilities.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.dto.mlbapi.match.DateEntry;
import com.mlb.mlbportal.dto.mlbapi.match.GameEntry;
import com.mlb.mlbportal.dto.mlbapi.match.ScheduleResponse;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.team.TeamService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchImportService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final TeamImportService teamLookupService;
    private final TeamService teamService;
    private final CacheService cacheService;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;
    private final Clock clock;

    private MatchImportService self;

    @Autowired
    public void setSelf(@Lazy MatchImportService self) {
        this.self = self;
    }

    /**
     * Obtain the matches for the current season in a period of time.
     *
     * @param start lower limit of the date interval.
     * @param end high limit of the date interval.
     *
     * @return the list of matches obtained for that period of time.
     */
    @Transactional
    @CircuitBreaker(name = "mlbApi", fallbackMethod = "fallbackMatches")
    @Retry(name = "mlbApi")
    public List<MatchDTO> getOfficialMatches(LocalDate start, LocalDate end) {
        String url = "https://statsapi.mlb.com/api/v1/schedule?sportId=1" +
                     "&startDate=" + start + "&endDate=" + end;

        ScheduleResponse response = this.restTemplate.getForObject(url, ScheduleResponse.class);

        if (response == null || response.dates() == null) {
            return List.of();
        }

        log.info("Matches successfully retrieved!");
        return response.dates().stream()
                .flatMap(d -> d.games().stream())
                .map(this::toMatchDTO)
                .filter(this::isMlbTeamMatch)
                .filter(dto -> this.stadiumRepository.findByName(dto.stadiumName()).isPresent())
                .map(this::saveMatch)
                .toList();
    }

    /**
     * Verify the status of a match periodically.
     * It manages the change of status a match could have, for example, from "IN PROGRESS" to "FINISHED"
     */
    @Transactional
    @CircuitBreaker(name = "verifyMatchStatus", fallbackMethod = "fallbackVerifyStatus")
    @Retry(name = "verifyMatchStatus")
    @CacheEvict(value = {
            "get-standings",
            "historic-ranking",
            "win-distribution",
            "wins-per-rivals",
            "get-home-matches",
            "get-away-matches"
    }, allEntries = true)
    public void verifyMatchStatus() {
        try {
            LocalDate today = LocalDate.now(this.clock);
            LocalDate yesterday = today.minusDays(1);
            LocalDate endDate = today.plusDays(1);
            String url = "https://statsapi.mlb.com/api/v1/schedule?sportId=1&startDate=" + yesterday + "&endDate=" + endDate;
            ScheduleResponse response = this.restTemplate.getForObject(url, ScheduleResponse.class);
            
            if (response == null || response.dates() == null) {
                log.info("No matches found for {}", today);
                return;
            }
            for (DateEntry dateEntry : response.dates()) {
                for (GameEntry gameEntry : dateEntry.games()) {
                    MatchDTO apiMatch = this.toMatchDTO(gameEntry);
                    this.matchRepository.findByStatsApiId(apiMatch.id()).ifPresent(match -> {
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
            log.info("Successfully updated the match for the {} games", today);
        }
        catch (Exception ex) {
            log.error("An error occur updating the status of the matches: {}", ex.getMessage());
        }
    }

    /**
     * Auxiliary method that update the status of the given match.
     *
     * @param match to update.
     */
    private void updateStatus(Match match) {
        Team awayTeam = match.getAwayTeam();
        Team homeTeam = match.getHomeTeam();
        if (this.isHomeTeamWinner(match)) {
            homeTeam.updateWins();
            awayTeam.updateLosses();
            match.setWinnerTeam(homeTeam);
        }
        else {
            awayTeam.updateWins();
            homeTeam.updateLosses();
            match.setWinnerTeam(awayTeam);
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

    /**
     * Store the obtained match in the database and return the dto of this stored match (with the id).
     *
     * @param dto the match to store
     *
     * @return the match with the id after storing in the database.
     */
    private MatchDTO saveMatch(MatchDTO dto) {
        return this.matchRepository.findByStatsApiId(dto.id()).map(existingMatch -> {
            existingMatch.setHomeScore(dto.homeScore());
            existingMatch.setAwayScore(dto.awayScore());
            existingMatch.setStatus(dto.status());
            existingMatch.setWinnerTeam(this.getWinnerTeam(dto));
            Match updated = this.matchRepository.save(existingMatch);
            return this.toMatchDTO(updated);
        }).orElseGet(() -> {
            Team homeEntity = this.teamRepository.findByNameOrThrow(dto.homeTeam().name());
            Team awayEntity = this.teamRepository.findByNameOrThrow(dto.awayTeam().name());
            Stadium stadium = this.stadiumRepository.findByNameOrThrow(dto.stadiumName());
            Match newMatch = new Match(
                    awayEntity,
                    homeEntity,
                    dto.awayScore(),
                    dto.homeScore(),
                    dto.date(),
                    dto.status()
            );
            newMatch.setWinnerTeam(this.getWinnerTeam(dto));
            newMatch.setStadium(stadium);
            newMatch.setStatsApiId(dto.id());
            Match saved = this.matchRepository.save(newMatch);
            return this.toMatchDTO(saved);
        });
    }

    private Team getWinnerTeam(MatchDTO matchDTO) {
        Team awayTeam = this.teamRepository.findByNameOrThrow(matchDTO.awayTeam().name());
        Team homeTeam = this.teamRepository.findByNameOrThrow(matchDTO.homeTeam().name());
        int awayScore = matchDTO.awayScore();
        int homeScore = matchDTO.homeScore();
        if (matchDTO.status().equals(MatchStatus.FINISHED)) {
            return (awayScore > homeScore) ? awayTeam : homeTeam;
        }
        return null;
    }

    @SuppressWarnings("unused")
    private List<MatchDTO> fallbackMatches(LocalDate start, LocalDate end, Throwable t) throws ServiceUnavailableException {
        log.error("Error loading the matches from {} to {}: {}", start, end, t.getMessage());
        return List.of();
    }

    @SuppressWarnings("unused")
    private void fallbackVerifyStatus(Throwable t) {
        log.error("verifyMatchStatus Failed{}", t.getMessage());
    }

    /**
     * Auxiliary method that filters the teams that are in the MLB.
     * This method is necessary because the API has information on more teams than just those in MLB.
     *
     * @param dto the match.
     *
     * @return true if the team belongs to the MLB, false otherwise.
     */
    private boolean isMlbTeamMatch(MatchDTO dto) {
        boolean homeExists = this.teamRepository.findByName(dto.homeTeam().name()).isPresent();
        boolean awayExists = this.teamRepository.findByName(dto.awayTeam().name()).isPresent();
        return homeExists && awayExists;
    }

    /**
     * Map the match data from the APi to the match data of the application.
     *
     * @param game match data from the API.
     *
     * @return match data of the application (mapped).
     */
    private MatchDTO toMatchDTO(GameEntry game) {
        int homeId = game.teams().home().team().id();
        int awayId = game.teams().away().team().id();
        TeamSummary home = this.teamLookupService.getTeamSummary(homeId);
        TeamSummary away = this.teamLookupService.getTeamSummary(awayId);
        LocalDateTime date = ZonedDateTime.parse(game.gameDate())
                .withZoneSameInstant(ZoneId.of("Europe/Madrid")).toLocalDateTime();
        MatchStatus status = this.mapStatus(game.status().detailedState());
        String stadiumName = game.venue().name();
        if (stadiumName.equals("Rate Field")) {
            stadiumName = "Guaranteed Rate Field";
        }
        return new MatchDTO(game.gamePk(), home, away, Objects.requireNonNullElse(game.teams().home().score(), 0),
                Objects.requireNonNullElse(game.teams().away().score(), 0), date, status, stadiumName);
    }

    /**
     * Map the match data from the APi to the match data of the application.
     *
     * @param match the match entity to map.
     *
     * @return match data of the application (mapped).
     */
    private MatchDTO toMatchDTO(Match match) {
        TeamSummary home = new TeamSummary(
                match.getHomeTeam().getName(),
                match.getHomeTeam().getAbbreviation(),
                match.getHomeTeam().getLeague(),
                match.getHomeTeam().getDivision()
        );
        TeamSummary away = new TeamSummary(
                match.getAwayTeam().getName(),
                match.getAwayTeam().getAbbreviation(),
                match.getAwayTeam().getLeague(),
                match.getAwayTeam().getDivision()
        );
        return new MatchDTO(
                match.getId(),
                home,
                away,
                match.getHomeScore(),
                match.getAwayScore(),
                match.getDate(),
                match.getStatus(),
                match.getStadium().getName()
        );
    }

    /**
     * Auxiliary method that map the status from the API to the status of the application
     *
     * @param apiStatus status from the API.
     *
     * @return status of the application (mapped).
     */
    private MatchStatus mapStatus(String apiStatus) {
        return switch (apiStatus) {
            case "In Progress" -> MatchStatus.IN_PROGRESS;
            case "Final" -> MatchStatus.FINISHED;
            default -> MatchStatus.SCHEDULED;
        };
    }

    @Async
    @Transactional
    public void updateSeasonMatches() {
        this.matchRepository.deleteAll();
        this.cacheService.clearCaches(
                "get-standings",
                "historic-ranking",
                "win-distribution",
                "wins-per-rivals",
                "get-home-matches",
                "get-away-matches"
        );
        this.self.getOfficialMatches(
                LocalDate.of(2026, Month.MARCH, 1),
                LocalDate.of(2026, Month.OCTOBER, 20)
        );
    }
}