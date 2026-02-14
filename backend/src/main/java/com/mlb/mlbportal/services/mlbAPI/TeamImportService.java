package com.mlb.mlbportal.services.mlbAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.naming.ServiceUnavailableException;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mlb.mlbportal.dto.mlbapi.team.TeamDetails;
import com.mlb.mlbportal.dto.mlbapi.team.TeamDetailsResponse;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;

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
        catch (Exception ex) {
            log.error("Cannot obtain the id from Stats API for the teams: {}", ex.getMessage());
        }
        return new HashMap<>();
    }

    @SuppressWarnings("unused")
    public Map<String, Integer> fallbackFindStatsAPiID(Throwable t) {
        log.error("Cannot obtain the stat API ID: {}", t.getMessage());
        return new HashMap<>();
    }
}