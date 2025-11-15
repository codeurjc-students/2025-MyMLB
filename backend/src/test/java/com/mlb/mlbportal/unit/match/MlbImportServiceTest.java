package com.mlb.mlbportal.unit.match;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.dto.mlbApi.*;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.services.MlbImportService;
import com.mlb.mlbportal.services.TeamLookupService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class MlbImportServiceTest {

    @Mock
    private TeamLookupService teamLookupService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MlbImportService mlbImportService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() throws Exception {
        var field = MlbImportService.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(mlbImportService, restTemplate);
    }

    @Test
    void testGetOfficialMatches() {
        LocalDate start = LocalDate.of(2026, 3, 1);
        LocalDate end = LocalDate.of(2026, 3, 1);

        TeamData homeTeamData = new TeamData(1, "Home Team", "HT");
        TeamData awayTeamData = new TeamData(2, "Away Team", "AT");

        TeamSide homeSide = new TeamSide(homeTeamData, 5);
        TeamSide awaySide = new TeamSide(awayTeamData, 3);

        Teams teams = new Teams(homeSide, awaySide);
        Status status = new Status("Final");

        GameEntry gameEntry = new GameEntry("2026-03-01T18:05:00", status, teams);
        DateEntry dateEntry = new DateEntry(List.of(gameEntry));
        ScheduleResponse scheduleResponse = new ScheduleResponse(List.of(dateEntry));

        when(restTemplate.getForObject(anyString(), eq(ScheduleResponse.class)))
                .thenReturn(scheduleResponse);

        when(teamLookupService.getTeamSummary(1))
                .thenReturn(new TeamSummary("Home Team", "HT", null, null));
        when(teamLookupService.getTeamSummary(2))
                .thenReturn(new TeamSummary("Away Team", "AT", null, null));

        List<MatchDTO> matches = mlbImportService.getOfficialMatches(start, end);

        assertThat(matches).isNotNull().hasSize(1);

        MatchDTO match = matches.get(0);

        assertThat(match.homeTeam().name()).isEqualTo("Home Team");
        assertThat(match.awayTeam().name()).isEqualTo("Away Team");
        assertThat(match.homeScore()).isEqualTo(5);
        assertThat(match.awayScore()).isEqualTo(3);
        assertThat(match.status()).isEqualTo(MatchStatus.FINISHED);
        assertThat(match.date()).isEqualTo(LocalDateTime.parse("2026-03-01T18:05:00"));
    }
}