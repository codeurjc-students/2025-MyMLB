package com.mlb.mlbportal.services.mlbAPI;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.naming.ServiceUnavailableException;

import com.mlb.mlbportal.dto.mlbapi.team.SplitRecords;
import com.mlb.mlbportal.models.DailyStandings;
import com.mlb.mlbportal.repositories.DailyStandingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.mlb.mlbportal.dto.mlbapi.team.Records;
import com.mlb.mlbportal.dto.mlbapi.team.StandingsResponse;
import com.mlb.mlbportal.dto.mlbapi.team.TeamDetails;
import com.mlb.mlbportal.dto.mlbapi.team.TeamDetailsResponse;
import com.mlb.mlbportal.dto.mlbapi.team.TeamRecords;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.repositories.TeamRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamImportService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<Integer, TeamSummary> cache = new HashMap<>();
    private final TeamRepository teamRepository;
    private final DailyStandingsRepository dailyStandingsRepository;

    private static final Map<String, Division> TEAM_DIVISIONS = Map.ofEntries(
            Map.entry("BAL", Division.EAST),
            Map.entry("BOS", Division.EAST),
            Map.entry("NYY", Division.EAST),
            Map.entry("TB", Division.EAST),
            Map.entry("TOR", Division.EAST),

            Map.entry("DET", Division.CENTRAL),
            Map.entry("CLE", Division.CENTRAL),
            Map.entry("MIN", Division.CENTRAL),
            Map.entry("KC", Division.CENTRAL),
            Map.entry("CWS", Division.CENTRAL),

            Map.entry("LAA", Division.WEST),
            Map.entry("OAK", Division.WEST),
            Map.entry("SEA", Division.WEST),
            Map.entry("HOU", Division.WEST),
            Map.entry("TEX", Division.WEST),

            Map.entry("WAS", Division.EAST),
            Map.entry("PHI", Division.EAST),
            Map.entry("ATL", Division.EAST),
            Map.entry("NYM", Division.EAST),
            Map.entry("MIA", Division.EAST),

            Map.entry("STL", Division.CENTRAL),
            Map.entry("MIL", Division.CENTRAL),
            Map.entry("CHC", Division.CENTRAL),
            Map.entry("CIN", Division.CENTRAL),
            Map.entry("PIT", Division.CENTRAL),

            Map.entry("LAD", Division.WEST),
            Map.entry("SF", Division.WEST),
            Map.entry("SD", Division.WEST),
            Map.entry("COL", Division.WEST),
            Map.entry("ARI", Division.WEST)
    );

    /**
     * Get the API team and assign one to an application team.
     *
     * @param teamId stats API team ID.
     *
     * @return the team in the application format.
     */
    @CircuitBreaker(name = "mlbApi", fallbackMethod = "fallbackTeams")
    @Retry(name = "mlbApi")
    public TeamSummary getTeamSummary(int teamId) {
        if (this.cache.containsKey(teamId)) {
            return this.cache.get(teamId);
        }
        String url = "https://statsapi.mlb.com/api/v1/teams/" + teamId;

        TeamDetailsResponse response = this.restTemplate.getForObject(url, TeamDetailsResponse.class);

        if (response == null || response.teams() == null || response.teams().isEmpty()) {
            return null;
        }

        TeamDetails t = response.teams().getFirst();

        League league = null;
        if (t.league() != null && t.league().name() != null) {
            league = switch (t.league().name()) {
                case "American League" -> League.AL;
                case "National League" -> League.NL;
                default -> null;
            };
        }

        Division division = TEAM_DIVISIONS.get(t.abbreviation());
        TeamSummary summary = new TeamSummary(
                t.name(),
                t.abbreviation(),
                league,
                division);

        this.cache.put(teamId, summary);
        return summary;
    }

    @SuppressWarnings("unused")
    private TeamSummary fallbackTeams(int teamId, Throwable t) throws ServiceUnavailableException {
        if (this.cache.containsKey(teamId)) {
            return this.cache.get(teamId);
        }
        throw new ServiceUnavailableException("MLB API not available and no cached data for teamId = " + teamId);
    }

    /**
     * Associate each team of the application with its corresponding stats API ID.
     *
     * @return said association in a map format.
     */
    @CircuitBreaker(name = "mlbApi", fallbackMethod = "fallbackFindStatsAPiID")
    @Retry(name = "mlbApi")
    public Map<String, Integer> findStatsApiId() {
        String url = "https://statsapi.mlb.com/api/v1/teams?sportId=1";
        try {
            TeamDetailsResponse response = this.restTemplate.getForObject(url, TeamDetailsResponse.class);
            if (response != null && response.teams() != null) {
                return response.teams().stream().collect(Collectors.toMap(
                        TeamDetails::name,
                        TeamDetails::id,
                        (existing, replacement) -> existing
                ));
            }
        }
        catch (RestClientException ex) {
            log.error("Cannot obtain the id from Stats API for the teams: {}", ex.getMessage());
        }
        return new HashMap<>();
    }

    @SuppressWarnings("unused")
    public Map<String, Integer> fallbackFindStatsAPiID(Throwable t) {
        log.error("Cannot obtain the stat API ID: {}", t.getMessage());
        return new HashMap<>();
    }

    /**
     * Obtain the team stats from the API, and store it in the database.
     * Also, stores the ranking data for today.
     */
    @Transactional
    @CircuitBreaker(name = "getTeamStats", fallbackMethod = "fallbackTeamStats")
    @Retry(name = "getTeamStats")
    public void getTeamStats() {
        int currentYear = LocalDate.now().getYear();
        LocalDate today = LocalDate.now();
        String url = "https://statsapi.mlb.com/api/v1/standings?leagueId=103,104&season="
                + currentYear + "&standingsTypes=regularSeason";

        try {
            StandingsResponse response = this.restTemplate.getForObject(url, StandingsResponse.class);
            if (response == null || response.records() == null || response.records().isEmpty()) {
                log.warn("No stats found for the {} season. Applying empty values", currentYear);
                this.setEmptyStats();
                return;
            }
            List<Team> teamsToSave = new ArrayList<>();
            List<DailyStandings> dailyHistory = new ArrayList<>();
            for (Records records : response.records()) {
                if (records.teamRecords() != null) {
                    for (TeamRecords teamRecord : records.teamRecords()) {
                        Team team = this.updateTeamStats(teamRecord);
                        teamsToSave.add(team);
                        if (!this.dailyStandingsRepository.existsByTeamAndMatchDate(team, today)) {
                            dailyHistory.add(new DailyStandings(
                                    team,
                                    today,
                                    this.parseRankToInt(teamRecord.divisionRank()),
                                    Objects.requireNonNullElse(teamRecord.wins(), 0),
                                    Objects.requireNonNullElse(teamRecord.losses(), 0)
                            ));
                        }
                    }
                }
            }
            this.teamRepository.saveAll(teamsToSave);
            this.dailyStandingsRepository.saveAll(dailyHistory);
            log.info("Rankings and Team Stats successfully updated!");
        }
        catch (Exception e) {
            log.error("Error obtaining the stats from the API: {}", e.getMessage());
            this.setEmptyStats();
        }
    }

    private Team updateTeamStats(TeamRecords teamRecord) {
        log.info("Team: {}", teamRecord.team().name());
        Team team = this.teamRepository.findByStatsApiIdOrThrow(teamRecord.team().id());
        team.addTeamStats(
                Objects.requireNonNullElse(teamRecord.gamesPlayed(), 0),
                Objects.requireNonNullElse(teamRecord.wins(), 0),
                Objects.requireNonNullElse(teamRecord.losses(), 0),
                this.parseGamesBehindAsDouble(teamRecord.divisionGamesBack()),
                Objects.requireNonNullElse(teamRecord.runsScored(), 0),
                Objects.requireNonNullElse(teamRecord.runsAllowed(), 0),
                Objects.requireNonNullElse(teamRecord.runDifferential(), 0),
                Objects.requireNonNullElse(teamRecord.winningPercentage(), ".000"),
                teamRecord.getLastTenGames()
        );
        SplitRecords homeSplits = teamRecord.getHomeSplit().orElse(new SplitRecords(0, 0, "home"));
        SplitRecords roadSplits = teamRecord.getAwaySplit().orElse(new SplitRecords(0, 0, "away"));

        team.setHomeGamesPlayed(homeSplits.getTotalGamesPlayed());
        team.setHomeGamesWins(homeSplits.wins());

        team.setRoadGamesPlayed(roadSplits.getTotalGamesPlayed());
        team.setRoadGamesWins(roadSplits.wins());
        return team;
    }

    /**
     * Obtain the gamesBehind value from the API as a String, and Map it to a double value.
     *
     * @param value gamesBehind value as a String.
     * @return gamesBehind value as a double.
     */
    private double parseGamesBehindAsDouble(String value) {
        if (value == null || value.equals("-")) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
        }
        catch (Exception ex) {
            return 0.0;
        }
    }

    private void setEmptyStats() {
        List<Team> teams = this.teamRepository.findAll();
        teams.forEach(team -> {
            team.addTeamStats(0, 0, 0, 0.0, 0, 0, 0, ".000", "0-0");
            team.setHomeGamesPlayed(0);
            team.setHomeGamesWins(0);
            team.setRoadGamesPlayed(0);
            team.setRoadGamesWins(0);
        });
        this.teamRepository.saveAll(teams);
    }

    public void fallbackTeamStats(Throwable throwable) {
        log.error("getTeamStats: Error fetching the team stats: {} ", throwable.getMessage());
    }

    /**
     * Triggers a full historical data synchronization from the start of the current season to today.
     */
    public void hydrateHistoryFromStart() {
        LocalDate startOfSeason = LocalDate.of(2026, 3, 25);
        LocalDate today = LocalDate.now();

        for (LocalDate date = startOfSeason; date.isBefore(today); date = date.plusDays(1)) {
            try {
                log.info("Updating Historic Ranking for date: {}", date);
                this.importStatsByDate(date);
                Thread.sleep(500);
            }
            catch (Exception e) {
                log.error("Error on date {}: {}", date, e.getMessage());
            }
        }
    }

    /**
     * Synchronizes daily rankings from the API for a specific date.
     *
     * @param date the season day to fetch and persist.
     */
    @CircuitBreaker(name = "importStatsByDate", fallbackMethod = "fallbackHistoricRanking")
    @Retry(name = "importStatsByDate")
    private void importStatsByDate(LocalDate date) {
        String url = String.format(
                "https://statsapi.mlb.com/api/v1/standings?leagueId=103,104&season=2026&date=%s",
                date.toString()
        );

        try {
            StandingsResponse response = this.restTemplate.getForObject(url, StandingsResponse.class);

            if (response != null && response.records() != null) {
                List<DailyStandings> dayHistory = new ArrayList<>();
                for (Records record : response.records()) {
                    if (record.teamRecords() == null) continue;

                    for (TeamRecords teamRecord : record.teamRecords()) {
                        this.teamRepository.findByStatsApiId(teamRecord.team().id()).ifPresent(team -> dayHistory.add(new DailyStandings(
                                team,
                                date,
                                this.parseRankToInt(teamRecord.divisionRank()),
                                teamRecord.wins(),
                                teamRecord.losses()
                        )));
                    }
                }
                if (!dayHistory.isEmpty()) {
                    this.dailyStandingsRepository.saveAll(dayHistory);
                    log.info("Stored {} registers for date {}", dayHistory.size(), date);
                }
            }
        }
        catch (Exception e) {
            log.warn("API failed for date {}", date);
            throw e;
        }
    }

    public void fallbackHistoricRanking(LocalDate date, Throwable t) {
        log.warn("hydrateHistoricRanking: An error occur for date {}: {}", date, t.getMessage());
    }

    /**
     * Parse the ranking from String (API) to Integer (Application)
     *
     * @param value ranking value from the API.
     * @return ranking value as Integer.
     */
    private int parseRankToInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }
}