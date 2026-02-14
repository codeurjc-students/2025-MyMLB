package com.mlb.mlbportal.unit.mlbApi;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.naming.ServiceUnavailableException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mlb.mlbportal.services.mlbAPI.MatchImportService;
import com.mlb.mlbportal.services.mlbAPI.TeamImportService;
import com.mlb.mlbportal.services.team.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import com.mlb.mlbportal.dto.mlbapi.Venue;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchImportServiceTest {

    @Mock
    private TeamImportService teamLookupService;

    @Mock
    private TeamService teamService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private StadiumRepository stadiumRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private MatchImportService mlbImportService;

    private List<Team> mockTeams;

    private List<TeamSummary> mockTeamSummaries;

    private List<Stadium> mockStadiums;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        var field = MatchImportService.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(this.mlbImportService, this.restTemplate);
        this.mockTeams = BuildMocksFactory.setUpTeamMocks();
        this.mockTeamSummaries = BuildMocksFactory.buildTeamSummaryMocks(this.mockTeams);
        this.mockStadiums = BuildMocksFactory.setUpStadiums();
    }

    @Test
    @DisplayName("Should save finished match when API returns valid data")
    void testGetOfficialMatchesFinishedMatchSaved() {
        TeamData homeTeamData = new TeamData(1, mockTeams.getFirst().getName(), mockTeams.getFirst().getAbbreviation());
        TeamData awayTeamData = new TeamData(2, mockTeams.get(1).getName(), mockTeams.get(1).getAbbreviation());

        TeamSide homeSide = new TeamSide(homeTeamData, 5);
        TeamSide awaySide = new TeamSide(awayTeamData, 3);

        Teams teams = new Teams(homeSide, awaySide);
        Status status = new Status("Final");

        String expectedStadiumName = this.mockStadiums.getFirst().getName();
        Venue venue = new Venue(1, expectedStadiumName);

        GameEntry gameEntry = new GameEntry("2026-03-01T18:05:00", status, teams, venue);
        DateEntry dateEntry = new DateEntry(List.of(gameEntry));
        ScheduleResponse scheduleResponse = new ScheduleResponse(List.of(dateEntry));

        when(this.restTemplate.getForObject(anyString(), eq(ScheduleResponse.class))).thenReturn(scheduleResponse);

        TeamSummary homeSummary = this.mockTeamSummaries.getFirst();
        TeamSummary awaySummary = this.mockTeamSummaries.get(1);

        Match savedMatchMock = new Match();
        savedMatchMock.setId(100L);

        when(this.teamLookupService.getTeamSummary(1)).thenReturn(homeSummary);
        when(this.teamLookupService.getTeamSummary(2)).thenReturn(awaySummary);
        when(this.teamRepository.findByName(mockTeams.getFirst().getName())).thenReturn(Optional.of(mockTeams.getFirst()));
        when(this.teamRepository.findByName(mockTeams.get(1).getName())).thenReturn(Optional.of(mockTeams.get(1)));
        when(this.stadiumRepository.findByName(expectedStadiumName)).thenReturn(Optional.of(this.mockStadiums.getFirst()));
        when(this.stadiumRepository.findByNameOrThrow(expectedStadiumName)).thenReturn(this.mockStadiums.getFirst());
        when(this.matchRepository.save(any(Match.class))).thenReturn(savedMatchMock);

        List<MatchDTO> matches = this.mlbImportService.getOfficialMatches(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 1));

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().status()).isEqualTo(MatchStatus.FINISHED);
        verify(this.matchRepository, times(1)).save(any(Match.class));
    }

    private ScheduleResponse buildResponse() {
        TeamData homeTeamData = new TeamData(1, TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION);
        TeamData awayTeamData = new TeamData(2, TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION);

        TeamSide homeSide = new TeamSide(homeTeamData, 2);
        TeamSide awaySide = new TeamSide(awayTeamData, 1);

        Teams teams = new Teams(homeSide, awaySide);
        Status status = new Status("Final");
        Venue venue = new Venue(1, STADIUM1_NAME);

        GameEntry gameEntry = new GameEntry("2026-03-01T18:05:00", status, teams, venue);
        DateEntry dateEntry = new DateEntry(List.of(gameEntry));

        return new ScheduleResponse(List.of(dateEntry));
    }

    @Test
    @DisplayName("Should throw TeamNotFoundException when team is missing in repository during saving")
    void testGetOfficialMatchesTeamNotFoundThrowsException() {
        ScheduleResponse scheduleResponse = this.buildResponse();
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 1);

        when(this.restTemplate.getForObject(anyString(), eq(ScheduleResponse.class))).thenReturn(scheduleResponse);
        when(this.teamLookupService.getTeamSummary(1)).thenReturn(this.mockTeamSummaries.getFirst());
        when(this.teamLookupService.getTeamSummary(2)).thenReturn(this.mockTeamSummaries.get(1));
        when(this.teamRepository.findByName(TEST_TEAM1_NAME)).thenReturn(Optional.of(this.mockTeams.getFirst()));
        when(this.teamRepository.findByName(TEST_TEAM2_NAME)).thenReturn(Optional.of(this.mockTeams.get(1)));
        when(this.stadiumRepository.findByName(STADIUM1_NAME)).thenReturn(Optional.of(new Stadium()));

        when(this.teamRepository.findByName(TEST_TEAM1_NAME)).thenReturn(Optional.of(this.mockTeams.getFirst())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.mlbImportService.getOfficialMatches(startDate, endDate))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage("Team Not Found");
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
        cachedMatch.setStadium(this.mockStadiums.getFirst());

        when(this.matchRepository.findByDateBetween(any(), any())).thenReturn(List.of(cachedMatch));

        var method = MatchImportService.class.getDeclaredMethod(
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
        assertThat(matches.getFirst().homeTeam().name()).isEqualTo(homeTeam.getName());
        assertThat(matches.getFirst().awayTeam().name()).isEqualTo(awayTeam.getName());
        assertThat(matches.getFirst().status()).isEqualTo(MatchStatus.FINISHED);
    }

    @Test
    @DisplayName("Should update match scores and status on API response")
    void testVerifyMatchStatusUpdatesScoresAndStatus() {
        LocalDate today = LocalDate.of(2026, 3, 1);

        when(this.clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(this.clock.instant()).thenReturn(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Match existingMatch = new Match();
        existingMatch.setId(1L);
        existingMatch.setStatus(MatchStatus.SCHEDULED);
        existingMatch.setHomeTeam(this.mockTeams.getFirst());
        existingMatch.setAwayTeam(this.mockTeams.get(1));
        existingMatch.setHomeScore(0);
        existingMatch.setAwayScore(0);

        ScheduleResponse response = this.buildResponse();
        when(this.restTemplate.getForObject(anyString(), eq(ScheduleResponse.class))).thenReturn(response);
        when(this.teamLookupService.getTeamSummary(1)).thenReturn(this.mockTeamSummaries.getFirst());
        when(this.teamLookupService.getTeamSummary(2)).thenReturn(this.mockTeamSummaries.get(1));
        when(this.matchRepository.findById(any())).thenReturn(Optional.of(existingMatch));

        this.mlbImportService.verifyMatchStatus();

        assertThat(existingMatch.getStatus()).isEqualTo(MatchStatus.FINISHED);
        assertThat(existingMatch.getHomeScore()).isEqualTo(2);
        assertThat(existingMatch.getAwayScore()).isEqualTo(1);

        verify(this.teamService, times(1)).updateRanking(any(Team.class), any(Team.class));
        verify(this.matchRepository, times(1)).save(existingMatch);
        verify(this.teamRepository, times(2)).save(any(Team.class));
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when no games are scheduled for today")
    void testVerifyMatchStatusNoGamesScheduled() {
        LocalDate today = LocalDate.of(2026, 3, 1);

        when(this.clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(this.clock.instant()).thenReturn(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        ScheduleResponse emptyResponse = new ScheduleResponse(null);
        when(this.restTemplate.getForObject(anyString(), eq(ScheduleResponse.class))).thenReturn(emptyResponse);

        assertThatThrownBy(() -> this.mlbImportService.verifyMatchStatus())
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No games scheduled for today");
    }

    @Test
    @DisplayName("Should not update ranking if status has not yet changed to FINISHED")
    void testVerifyMatchStatusNoStatusChange() {
        LocalDate today = LocalDate.of(2026, 3, 1);

        when(this.clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(this.clock.instant()).thenReturn(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Match existingMatch = new Match();
        existingMatch.setStatus(MatchStatus.FINISHED);
        existingMatch.setHomeScore(2);
        existingMatch.setAwayScore(1);

        when(this.restTemplate.getForObject(anyString(), eq(ScheduleResponse.class))).thenReturn(this.buildResponse());
        when(this.teamLookupService.getTeamSummary(anyInt())).thenReturn(this.mockTeamSummaries.getFirst());
        when(this.matchRepository.findById(any())).thenReturn(Optional.of(existingMatch));

        this.mlbImportService.verifyMatchStatus();

        verify(this.teamService, never()).updateRanking(any(), any());
        verify(this.matchRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ServiceUnavailableException when API fails and no cache is available")
    void testFallbackMatchesThrowsServiceUnavailableWhenNoCache() throws Exception {
        when(this.matchRepository.findByDateBetween(any(), any())).thenReturn(List.of());

        var method = MatchImportService.class.getDeclaredMethod(
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