package com.mlb.mlbportal.unit.match;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.naming.ServiceUnavailableException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.dto.mlbapi.DateEntry;
import com.mlb.mlbportal.dto.mlbapi.GameEntry;
import com.mlb.mlbportal.dto.mlbapi.ScheduleResponse;
import com.mlb.mlbportal.dto.mlbapi.Status;
import com.mlb.mlbportal.dto.mlbapi.TeamData;
import com.mlb.mlbportal.dto.mlbapi.TeamSide;
import com.mlb.mlbportal.dto.mlbapi.Teams;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.MlbImportService;
import com.mlb.mlbportal.services.TeamLookupService;
import com.mlb.mlbportal.utils.BuildMocksFactory;

@ExtendWith(MockitoExtension.class)
class MlbImportServiceTest {

    @Mock
    private TeamLookupService teamLookupService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private MlbImportService mlbImportService;

    private List<Team> mockTeams;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() throws Exception {
        var field = MlbImportService.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(this.mlbImportService, this.restTemplate);
        this.mockTeams = BuildMocksFactory.setUpTeamMocks();
    }

    @Test
    @DisplayName("Should save finished match when API returns valid data")
    void testGetOfficialMatchesFinishedMatchSaved() {
        TeamData homeTeamData = new TeamData(1, mockTeams.get(0).getName(), mockTeams.get(0).getAbbreviation());
        TeamData awayTeamData = new TeamData(2, mockTeams.get(1).getName(), mockTeams.get(1).getAbbreviation());

        TeamSide homeSide = new TeamSide(homeTeamData, 5);
        TeamSide awaySide = new TeamSide(awayTeamData, 3);

        Teams teams = new Teams(homeSide, awaySide);
        Status status = new Status("Final");
        GameEntry gameEntry = new GameEntry("2026-03-01T18:05:00", status, teams);
        DateEntry dateEntry = new DateEntry(List.of(gameEntry));
        ScheduleResponse scheduleResponse = new ScheduleResponse(List.of(dateEntry));

        when(this.restTemplate.getForObject(anyString(), eq(ScheduleResponse.class)))
                .thenReturn(scheduleResponse);

        when(this.teamLookupService.getTeamSummary(1))
                .thenReturn(
                        new TeamSummary(this.mockTeams.get(0).getName(), this.mockTeams.get(0).getAbbreviation(), null,
                                null));
        when(this.teamLookupService.getTeamSummary(2))
                .thenReturn(
                        new TeamSummary(this.mockTeams.get(1).getName(), this.mockTeams.get(1).getAbbreviation(), null,
                                null));

        when(this.teamRepository.findByName(mockTeams.get(0).getName()))
                .thenReturn(Optional.of(mockTeams.get(0)));
        when(this.teamRepository.findByName(mockTeams.get(1).getName()))
                .thenReturn(Optional.of(mockTeams.get(1)));

        List<MatchDTO> matches = this.mlbImportService.getOfficialMatches(LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 1));

        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).status()).isEqualTo(MatchStatus.FINISHED);

        verify(this.matchRepository, times(1)).save(any());
    }

    private ScheduleResponse buildResponse(String statusText, Integer homeScore, Integer awayScore) {
        TeamData homeTeamData = new TeamData(1, "Home Team", "HT");
        TeamData awayTeamData = new TeamData(2, "Away Team", "AT");

        TeamSide homeSide = new TeamSide(homeTeamData, homeScore);
        TeamSide awaySide = new TeamSide(awayTeamData, awayScore);

        Teams teams = new Teams(homeSide, awaySide);
        Status status = new Status(statusText);

        GameEntry gameEntry = new GameEntry("2026-03-01T18:05:00", status, teams);
        DateEntry dateEntry = new DateEntry(List.of(gameEntry));

        return new ScheduleResponse(List.of(dateEntry));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Should throw TeamNotFoundException when team is missing in repository")
    void testGetOfficialMatchesTeamNotFoundThrowsException() {
        ScheduleResponse scheduleResponse = this.buildResponse("Final", 2, 1);
        when(this.restTemplate.getForObject(anyString(), eq(ScheduleResponse.class)))
                .thenReturn(scheduleResponse);

        when(this.teamLookupService.getTeamSummary(1)).thenReturn(
                new TeamSummary(this.mockTeams.get(0).getName(), this.mockTeams.get(0).getAbbreviation(), null, null));
        when(this.teamLookupService.getTeamSummary(2)).thenReturn(
                new TeamSummary(this.mockTeams.get(1).getName(), this.mockTeams.get(1).getAbbreviation(), null, null));

        when(this.teamRepository.findByName(this.mockTeams.get(0).getName()))
                .thenReturn(Optional.of(this.mockTeams.get(0)), Optional.empty());
        when(this.teamRepository.findByName(this.mockTeams.get(1).getName()))
                .thenReturn(Optional.of(this.mockTeams.get(1)));

        assertThatThrownBy(
                () -> this.mlbImportService.getOfficialMatches(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 1)))
                .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    @DisplayName("Should return cached matches when API fails")
    void testFallbackMatchesReturnsCachedMatches() throws Exception {
        Team homeTeam = mockTeams.getFirst();
        Team awayTeam = mockTeams.get(1);

        Match cachedMatch = new Match(
                awayTeam,
                homeTeam,
                2,
                5,
                LocalDate.of(2026, 3, 1).atStartOfDay(),
                MatchStatus.FINISHED);

        when(this.matchRepository.findByDateBetween(any(), any())).thenReturn(List.of(cachedMatch));

        var method = MlbImportService.class.getDeclaredMethod(
                "fallbackMatches", LocalDate.class, LocalDate.class, Throwable.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<MatchDTO> matches = (List<MatchDTO>) method.invoke(
                this.mlbImportService,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 1),
                new RuntimeException("API MLB down")
        );

        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).homeTeam().name()).isEqualTo(homeTeam.getName());
        assertThat(matches.get(0).awayTeam().name()).isEqualTo(awayTeam.getName());
        assertThat(matches.get(0).status()).isEqualTo(MatchStatus.FINISHED);
    }

    @Test
    @DisplayName("Should throw ServiceUnavailableException when API fails and no cache is available")
    void testFallbackMatchesThrowsServiceUnavailableWhenNoCache() throws Exception {
        when(this.matchRepository.findByDateBetween(any(), any())).thenReturn(List.of());

        var method = MlbImportService.class.getDeclaredMethod(
                "fallbackMatches", LocalDate.class, LocalDate.class, Throwable.class);
        method.setAccessible(true);

        assertThatThrownBy(() -> method.invoke(
                this.mlbImportService,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 1),
                new RuntimeException("API MLB down")
        )).satisfies(throwable -> assertThat(throwable.getCause())
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("MLB API not available and without cached data"));
    }
}