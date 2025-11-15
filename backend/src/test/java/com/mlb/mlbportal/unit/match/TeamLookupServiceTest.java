package com.mlb.mlbportal.unit.match;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.mlb.mlbportal.dto.mlbApi.TeamDetails;
import com.mlb.mlbportal.dto.mlbApi.TeamDetailsResponse;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.services.TeamLookupService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class TeamLookupServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TeamLookupService teamLookupService;

    @Test
    void testGetTeamSummaryFromApi() {
        int teamId = 1;

        TeamDetails apiTeam = new TeamDetails(
                1,
                "New York Yankees",
                "NYY",
                new com.mlb.mlbportal.dto.mlbApi.LeagueInfo("American League"),
                null);
        TeamDetailsResponse response = new TeamDetailsResponse(List.of(apiTeam));

        when(restTemplate.getForObject("https://statsapi.mlb.com/api/v1/teams/" + teamId,
                TeamDetailsResponse.class)).thenReturn(response);

        TeamSummary summary = teamLookupService.getTeamSummary(teamId);

        assertThat(summary).isNotNull();
        assertThat(summary.name()).isEqualTo("New York Yankees");
        assertThat(summary.abbreviation()).isEqualTo("NYY");
        assertThat(summary.league()).isEqualTo(League.AL);
        assertThat(summary.division()).isEqualTo(Division.EAST);
    }

    @Test
    void testGetTeamSummaryCache() {
        int teamId = 1;

        TeamDetails apiTeam = new TeamDetails(
                1,
                "New York Yankees",
                "NYY",
                new com.mlb.mlbportal.dto.mlbApi.LeagueInfo("American League"),
                null);
        TeamDetailsResponse response = new TeamDetailsResponse(List.of(apiTeam));

        when(restTemplate.getForObject(anyString(), eq(TeamDetailsResponse.class)))
                .thenReturn(response);

        TeamSummary firstCall = teamLookupService.getTeamSummary(teamId);
        TeamSummary secondCall = teamLookupService.getTeamSummary(teamId);

        assertThat(secondCall).isSameAs(firstCall);

        verify(restTemplate, times(1)).getForObject(anyString(), eq(TeamDetailsResponse.class));
    }

    @Test
    void testGetTeamSummaryNullResponse() {
        int teamId = 99;
        when(restTemplate.getForObject(anyString(), eq(TeamDetailsResponse.class))).thenReturn(null);

        TeamSummary summary = teamLookupService.getTeamSummary(teamId);

        assertThat(summary).isNull();
    }

    @Test
    void testGetTeamSummaryUnknownLeague() {
        int teamId = 2;

        TeamDetails apiTeam = new TeamDetails(
                2,
                "Unknown Team",
                "XYZ",
                new com.mlb.mlbportal.dto.mlbApi.LeagueInfo("Some League"),
                null);
        TeamDetailsResponse response = new TeamDetailsResponse(List.of(apiTeam));

        when(restTemplate.getForObject(anyString(), eq(TeamDetailsResponse.class))).thenReturn(response);

        TeamSummary summary = teamLookupService.getTeamSummary(teamId);

        assertThat(summary).isNotNull();
        assertThat(summary.league()).isNull();
        assertThat(summary.division()).isNull();
    }
}