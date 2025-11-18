package com.mlb.mlbportal.unit.match;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.naming.ServiceUnavailableException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.mlb.mlbportal.dto.mlbapi.LeagueInfo;
import com.mlb.mlbportal.dto.mlbapi.TeamDetails;
import com.mlb.mlbportal.dto.mlbapi.TeamDetailsResponse;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.services.TeamLookupService;

@ExtendWith(MockitoExtension.class)
class TeamLookupServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TeamLookupService teamLookupService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() throws Exception {
        var field = TeamLookupService.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(this.teamLookupService, this.restTemplate);
    }

    @Test
    @DisplayName("Should fetch team summary from MLB API successfully")
    void testGetTeamSummaryFromApi() {
        int teamId = 1;

        TeamDetails apiTeam = new TeamDetails(
                1,
                "New York Yankees",
                "NYY",
                new LeagueInfo("American League"),
                null);
        TeamDetailsResponse response = new TeamDetailsResponse(List.of(apiTeam));

        when(this.restTemplate.getForObject("https://statsapi.mlb.com/api/v1/teams/" + teamId,
                TeamDetailsResponse.class)).thenReturn(response);

        TeamSummary summary = this.teamLookupService.getTeamSummary(teamId);

        assertThat(summary).isNotNull();
        assertThat(summary.name()).isEqualTo("New York Yankees");
        assertThat(summary.abbreviation()).isEqualTo("NYY");
        assertThat(summary.league()).isEqualTo(League.AL);
        assertThat(summary.division()).isEqualTo(Division.EAST);
    }

    @Test
    @DisplayName("Should return cached team summary on subsequent calls")
    void testGetTeamSummaryCache() {
        int teamId = 1;

        TeamDetails apiTeam = new TeamDetails(
                1,
                "New York Yankees",
                "NYY",
                new LeagueInfo("American League"),
                null);
        TeamDetailsResponse response = new TeamDetailsResponse(List.of(apiTeam));

        when(this.restTemplate.getForObject(anyString(), eq(TeamDetailsResponse.class)))
                .thenReturn(response);

        TeamSummary firstCall = this.teamLookupService.getTeamSummary(teamId);
        TeamSummary secondCall = this.teamLookupService.getTeamSummary(teamId);

        assertThat(secondCall).isSameAs(firstCall);

        verify(this.restTemplate, times(1)).getForObject(anyString(), eq(TeamDetailsResponse.class));
    }

    @Test
    @DisplayName("Should return null when API response is empty")
    void testGetTeamSummaryNullResponse() {
        int teamId = 99;
        when(this.restTemplate.getForObject(anyString(), eq(TeamDetailsResponse.class))).thenReturn(null);

        TeamSummary summary = this.teamLookupService.getTeamSummary(teamId);

        assertThat(summary).isNull();
    }

    @Test
    @DisplayName("Should handle unknown league gracefully with null values")
    void testGetTeamSummaryUnknownLeague() {
        int teamId = 2;

        TeamDetails apiTeam = new TeamDetails(
                2,
                "Unknown Team",
                "XYZ",
                new LeagueInfo("Some League"),
                null);
        TeamDetailsResponse response = new TeamDetailsResponse(List.of(apiTeam));

        when(this.restTemplate.getForObject(anyString(), eq(TeamDetailsResponse.class))).thenReturn(response);

        TeamSummary summary = this.teamLookupService.getTeamSummary(teamId);

        assertThat(summary).isNotNull();
        assertThat(summary.league()).isNull();
        assertThat(summary.division()).isNull();
    }

    @Test
    @DisplayName("Fallback should return cached team when API fails")
    void testFallbackTeamsReturnsCachedTeam() throws Exception {
        int teamId = 1;
        TeamSummary cachedSummary = new TeamSummary("Cached Team", "CT", League.AL, Division.EAST);

        var cacheField = TeamLookupService.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, TeamSummary> cache = (Map<Integer, TeamSummary>) cacheField.get(this.teamLookupService);
        cache.put(teamId, cachedSummary);

        Method method = TeamLookupService.class.getDeclaredMethod("fallbackTeams", int.class, Throwable.class);
        method.setAccessible(true);

        TeamSummary result = (TeamSummary) method.invoke(this.teamLookupService, teamId, new RuntimeException("API down"));

        assertThat(result).isEqualTo(cachedSummary);
    }

    @Test
    @DisplayName("Fallback should throw ServiceUnavailableException when API fails and no cache exists")
    void testFallbackTeamsThrowsServiceUnavailableWhenNoCache() throws Exception {
        int teamId = 99;

        var cacheField = TeamLookupService.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, TeamSummary> cache = (Map<Integer, TeamSummary>) cacheField.get(this.teamLookupService);
        cache.clear();

        Method method = TeamLookupService.class.getDeclaredMethod("fallbackTeams", int.class, Throwable.class);
        method.setAccessible(true);

        assertThatThrownBy(() -> method.invoke(this.teamLookupService, teamId, new RuntimeException("API down")))
            .satisfies(throwable -> assertThat(throwable.getCause())
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("MLB API not available and no cached data for teamId = " + teamId));
    }
}