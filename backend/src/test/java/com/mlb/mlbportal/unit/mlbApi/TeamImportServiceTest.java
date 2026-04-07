package com.mlb.mlbportal.unit.mlbApi;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.ServiceUnavailableException;

import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_LOGO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mlb.mlbportal.dto.mlbapi.team.*;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.mlbAPI.TeamImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.mlb.mlbportal.dto.mlbapi.match.LeagueInfo;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;

@ExtendWith(MockitoExtension.class)
class TeamImportServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TeamImportService teamImportService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() throws Exception {
        var field = TeamImportService.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(this.teamImportService, this.restTemplate);
    }

    @Test
    @DisplayName("Should get the team from Stats API successfully")
    void testGetTeamSummaryFromApi() {
        int teamId = 1;
        TeamDetails apiTeam = new TeamDetails(
                teamId,
                "New York Yankees",
                "NYY",
                new LeagueInfo("American League"),
                null);
        TeamDetailsResponse response = new TeamDetailsResponse(List.of(apiTeam));

        when(this.restTemplate.getForObject("https://statsapi.mlb.com/api/v1/teams/" + teamId, TeamDetailsResponse.class)).thenReturn(response);

        TeamSummary summary = this.teamImportService.getTeamSummary(teamId);

        assertThat(summary).isNotNull();
        assertThat(summary.name()).isEqualTo("New York Yankees");
        assertThat(summary.abbreviation()).isEqualTo("NYY");
        assertThat(summary.league()).isEqualTo(League.AL);
        assertThat(summary.division()).isEqualTo(Division.EAST);
    }

    @Test
    @DisplayName("Should return cached team summary")
    void testGetTeamSummaryCache() {
        int teamId = 1;

        TeamDetails apiTeam = new TeamDetails(
                teamId,
                TEST_TEAM1_NAME,
                TEST_TEAM1_ABBREVIATION,
                new LeagueInfo("American League"),
                null);
        TeamDetailsResponse response = new TeamDetailsResponse(List.of(apiTeam));

        when(this.restTemplate.getForObject(anyString(), eq(TeamDetailsResponse.class))).thenReturn(response);

        TeamSummary firstCall = this.teamImportService.getTeamSummary(teamId);
        TeamSummary secondCall = this.teamImportService.getTeamSummary(teamId);

        assertThat(secondCall).isSameAs(firstCall);

        verify(this.restTemplate, times(1)).getForObject(anyString(), eq(TeamDetailsResponse.class));
    }

    @Test
    @DisplayName("Should return null when API response is empty")
    void testGetTeamSummaryNullResponse() {
        int teamId = 99;
        when(this.restTemplate.getForObject(anyString(), eq(TeamDetailsResponse.class))).thenReturn(null);

        TeamSummary summary = this.teamImportService.getTeamSummary(teamId);

        assertThat(summary).isNull();
    }

    @Test
    @DisplayName("Should handle unknown league gracefully with null values")
    void testGetTeamSummaryUnknownLeague() {
        int teamId = 2;

        TeamDetails apiTeam = new TeamDetails(
                2,
                UNKNOWN_TEAM,
                UNKNOWN_TEAM,
                new LeagueInfo("Some League"),
                null);
        TeamDetailsResponse response = new TeamDetailsResponse(List.of(apiTeam));

        when(this.restTemplate.getForObject(anyString(), eq(TeamDetailsResponse.class))).thenReturn(response);

        TeamSummary summary = this.teamImportService.getTeamSummary(teamId);

        assertThat(summary).isNotNull();
        assertThat(summary.league()).isNull();
        assertThat(summary.division()).isNull();
    }

    @Test
    @DisplayName("Fallback should return cached team when API fails")
    void testFallbackTeamsReturnsCachedTeam() throws Exception {
        int teamId = 1;
        TeamSummary cachedSummary = new TeamSummary(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, League.AL, Division.EAST);

        var cacheField = TeamImportService.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, TeamSummary> cache = (Map<Integer, TeamSummary>) cacheField.get(this.teamImportService);
        cache.put(teamId, cachedSummary);

        Method method = TeamImportService.class.getDeclaredMethod("fallbackTeams", int.class, Throwable.class);
        method.setAccessible(true);

        TeamSummary result = (TeamSummary) method.invoke(this.teamImportService, teamId, new RuntimeException("API down"));

        assertThat(result).isEqualTo(cachedSummary);
    }

    @Test
    @DisplayName("Fallback should throw ServiceUnavailableException when API fails and no cache exists")
    void testFallbackTeamsThrowsServiceUnavailableWhenNoCache() throws Exception {
        int teamId = 99;

        var cacheField = TeamImportService.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, TeamSummary> cache = (Map<Integer, TeamSummary>) cacheField.get(this.teamImportService);
        cache.clear();

        Method method = TeamImportService.class.getDeclaredMethod("fallbackTeams", int.class, Throwable.class);
        method.setAccessible(true);

        assertThatThrownBy(() -> method.invoke(this.teamImportService, teamId, new RuntimeException("API down")))
            .satisfies(throwable -> assertThat(throwable.getCause())
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessage("MLB API not available and no cached data for teamId = " + teamId));
    }

    @Test
    @DisplayName("Should obtain the stats API ID for the team and return the association successfully")
    void testFindStatsAPIId() {
        TeamDetails apiTeam = new TeamDetails(
                1,
                TEST_TEAM1_NAME,
                TEST_TEAM1_ABBREVIATION,
                new LeagueInfo("American League"),
                null);
        TeamDetailsResponse response = new TeamDetailsResponse(List.of(apiTeam));

        when(this.restTemplate.getForObject("https://statsapi.mlb.com/api/v1/teams?sportId=1", TeamDetailsResponse.class)).thenReturn(response);

        Map<String, Integer> result = this.teamImportService.findStatsApiId();

        assertThat(result).hasSize(1);
        assertThat(result.containsKey(TEST_TEAM1_NAME)).isTrue();
        assertThat(result.get(TEST_TEAM1_NAME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Should obtain team stats from API")
    void testGetTeamStats() {
        int currentYear = LocalDate.now().getYear();
        String url = "https://statsapi.mlb.com/api/v1/standings?leagueId=103,104&season="
                + currentYear + "&standingsTypes=regularSeason";

        TeamRecordsGeneralInfo apiTeam = new TeamRecordsGeneralInfo(1L, TEST_TEAM1_NAME);
        TeamRecords teamRecord = new TeamRecords(
                apiTeam, "1",  10, 6, 4, ".600", "2.0", 42, 10, 32, new RecordsWrapper(new ArrayList<>())
        );
        Records records = new Records(List.of(teamRecord));
        StandingsResponse response = new StandingsResponse(List.of(records));

        Team domainTeam = new Team(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, League.AL, Division.EAST, TEST_TEAM1_LOGO);

        when(this.restTemplate.getForObject(url, StandingsResponse.class)).thenReturn(response);
        when(this.teamRepository.findByStatsApiIdOrThrow(1L)).thenReturn(domainTeam);

        this.teamImportService.getTeamStats();

        assertThat(domainTeam.getWins()).isEqualTo(6);
        assertThat(domainTeam.getPct()).isEqualTo(".600");
        verify(this.teamRepository, times(1)).findByStatsApiIdOrThrow(1L);
        verify(this.teamRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should handle empty records by setting empty stats")
    void testGetTeamStatsEmptyRecords() {
        int currentYear = LocalDate.now().getYear();
        String url = "https://statsapi.mlb.com/api/v1/standings?leagueId=103,104&season="
                + currentYear + "&standingsTypes=regularSeason";

        StandingsResponse response = new StandingsResponse(List.of());
        Team domainTeam = new Team(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, League.AL, Division.EAST, TEST_TEAM1_LOGO);

        when(this.restTemplate.getForObject(url, StandingsResponse.class)).thenReturn(response);
        when(this.teamRepository.findAll()).thenReturn(List.of(domainTeam));

        this.teamImportService.getTeamStats();

        assertThat(domainTeam.getWins()).isZero();
        assertThat(domainTeam.getPct()).isEqualTo(".000");
        verify(this.teamRepository).findAll();
        verify(this.teamRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should parse games behind string to double correctly")
    void testParseGamesBehindAsDouble() throws Exception {
        Method method = TeamImportService.class.getDeclaredMethod("parseGamesBehindAsDouble", String.class);
        method.setAccessible(true);

        double validResult = (double) method.invoke(this.teamImportService, "2.5");
        assertThat(validResult).isEqualTo(2.5);

        double dashResult = (double) method.invoke(this.teamImportService, "-");
        assertThat(dashResult).isZero();

        double nullResult = (double) method.invoke(this.teamImportService, (String) null);
        assertThat(nullResult).isZero();
    }
}