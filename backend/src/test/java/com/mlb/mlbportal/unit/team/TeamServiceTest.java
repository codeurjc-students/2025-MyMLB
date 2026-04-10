package com.mlb.mlbportal.unit.team;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.mlb.mlbportal.utils.TestConstants.OCCUPIED_STADIUM;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_USERNAME;
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_STADIUM;
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_TEAM;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mlb.mlbportal.dto.team.HistoricRankingDTO;
import com.mlb.mlbportal.dto.team.RunsStatsDTO;
import com.mlb.mlbportal.dto.team.WinDistributionDTO;
import com.mlb.mlbportal.dto.team.WinsPerRivalDTO;
import com.mlb.mlbportal.handler.badRequest.InvalidTypeException;
import com.mlb.mlbportal.handler.notFound.StadiumNotFoundException;
import com.mlb.mlbportal.mappers.DailyStandingsMapper;
import com.mlb.mlbportal.models.DailyStandings;
import com.mlb.mlbportal.repositories.DailyStandingsRepository;
import com.mlb.mlbportal.repositories.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.dto.team.TeamInfoDTO;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.dto.team.UpdateTeamRequest;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.mappers.TeamMapper;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.UserService;
import com.mlb.mlbportal.services.team.TeamService;
import com.mlb.mlbportal.services.utilities.PaginationHandlerService;
import com.mlb.mlbportal.utils.BuildMocksFactory;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private UserService userService;

    @Mock
    private PaginationHandlerService paginationHandlerService;

    @Mock
    private StadiumRepository stadiumRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private DailyStandingsRepository dailyStandingsRepository;

    @Mock
    private DailyStandingsMapper dailyStandingsMapper;

    @InjectMocks
    private TeamService teamService;

    private Team team1, team2, team3;
    private List<TeamDTO> mockTeamDTOs;
    private List<TeamInfoDTO> mockTeamInfoDTOs;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        List<Team> teams = BuildMocksFactory.setUpTeamMocks();
        this.team1 = teams.getFirst();
        this.team2 = teams.get(1);
        this.team3 = teams.get(2);

        this.mockTeamDTOs = BuildMocksFactory.buildTeamDTOMocks(teams);
        this.mockTeamInfoDTOs = BuildMocksFactory.buildTeamInfoDTOMocks(teams);
    }

    @Test
    @DisplayName("Should return all teams mapped correctly as DTOs")
    void testGetAllTeams() {
        List<Team> mockTeams = List.of(this.team1, this.team2, this.team3);
        Page<TeamInfoDTO> mockPage = new PageImpl<>(this.mockTeamInfoDTOs, PageRequest.of(0, 10), mockTeams.size());

        when(this.teamRepository.findAll()).thenReturn(mockTeams);
        doReturn(mockPage).when(this.paginationHandlerService).paginateAndMap(eq(mockTeams), eq(0), eq(10), any());

        Page<TeamInfoDTO> result = this.teamService.getTeams(0, 10);

        assertThat(result.getContent()).hasSize(3).containsExactlyElementsOf(this.mockTeamInfoDTOs);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return all available teams paginated and sorted")
    void testGetAvailableTeams() {
        List<Team> mockTeams = Arrays.asList(this.team1, this.team2, this.team3);
        List<TeamSummary> teamSummaries = BuildMocksFactory.buildTeamSummaryMocks(mockTeams);
        Page<TeamSummary> mockPage = new PageImpl<>(teamSummaries, PageRequest.of(0, 10), mockTeams.size());

        when(teamRepository.findAvailableTeams()).thenReturn(mockTeams);
        doReturn(mockPage).when(this.paginationHandlerService).paginateAndMap(eq(mockTeams), eq(0), eq(10), any());

        Page<TeamSummary> result = this.teamService.getAvailableTeams(0, 10);

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3).containsExactlyElementsOf(teamSummaries);
    }

    @Test
    @DisplayName("Should return the team")
    void testGetTeam() {
        when(this.teamRepository.findByNameOrThrow(TEST_TEAM1_NAME)).thenReturn(this.team1);
        assertThatNoException().isThrownBy(() -> this.teamService.getTeam(TEST_TEAM1_NAME));

        Team result = this.teamService.getTeam(TEST_TEAM1_NAME);
        assertThat(result.getName()).isEqualTo(TEST_TEAM1_NAME);
        assertThat(result.getAbbreviation()).isEqualTo(this.team1.getAbbreviation());
    }

    @Test
    @DisplayName("Should throw TeamNotFound for an unknown player")
    void testGetUnknownTeam() {
        when(this.teamRepository.findByNameOrThrow(UNKNOWN_TEAM)).thenCallRealMethod();

        assertThatThrownBy(() -> this.teamService.getTeam(UNKNOWN_TEAM))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage("Team Not Found");
    }

    @Test
    @DisplayName("Should return the general info of a team with updated players and pitchers")
    void testGetTeamInfo() {
        when(this.teamRepository.findByNameOrThrow(TEST_TEAM1_NAME)).thenReturn(this.team1);

        TeamInfoDTO expected = this.mockTeamInfoDTOs.getFirst();
        when(this.teamMapper.toTeamInfoDTO(this.team1)).thenReturn(expected);

        TeamInfoDTO result = this.teamService.getTeamInfo(TEST_TEAM1_NAME);

        assertThat(result).isNotNull();
        assertThat(result.teamStats().name()).isEqualTo(expected.teamStats().name());
        assertThat(result.teamStats().abbreviation()).isEqualTo(expected.teamStats().abbreviation());
        assertThat(result.city()).isEqualTo(expected.city());
        assertThat(result.stadium().name()).isEqualTo(expected.stadium().name());
    }

    @Test
    @DisplayName("Should throw TeamNotFoundException for a non existent team")
    void testGetNonExistentTeamInfo() {
        when(this.teamRepository.findByNameOrThrow(any())).thenCallRealMethod();

        assertThatThrownBy(() -> this.teamService.getTeamInfo(UNKNOWN_TEAM))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage("Team Not Found");
    }

    @Test
    @DisplayName("Should return standings grouped and ordered prioritizing user's favorite divisions")
    void testGetStandingsWithUserFavorites() {
        this.mockTeamRepositoryAndMapper();

        UserEntity mockUser = BuildMocksFactory.setUpUsers().getLast();
        mockUser.setFavTeams(new HashSet<>(List.of(this.team1, this.team3)));
        when(this.userService.getUser(TEST_USER_USERNAME)).thenReturn(mockUser);

        Map<League, Map<Division, List<TeamDTO>>> standings = this.teamService.getStandings(TEST_USER_USERNAME);

        this.assertCommonStandingsStructure(standings);

        assertThat(standings.get(League.AL).get(Division.EAST)).containsExactly(this.mockTeamDTOs.getFirst(), this.mockTeamDTOs.get(1));
        assertThat(standings.get(this.team3.getLeague()).get(this.team3.getDivision())).containsExactly(this.mockTeamDTOs.get(2));
    }

    @Test
    @DisplayName("Should return standings grouped and ordered correctly when no user is provided")
    void testGetStandingsWithoutUserFavorites() {
        this.mockTeamRepositoryAndMapper();

        Map<League, Map<Division, List<TeamDTO>>> standings = this.teamService.getStandings(null);

        this.assertCommonStandingsStructure(standings);

        assertThat(standings.get(League.AL).get(Division.EAST)).containsExactly(this.mockTeamDTOs.getFirst(), this.mockTeamDTOs.get(1));
        assertThat(standings.get(this.team3.getLeague()).get(this.team3.getDivision())).containsExactly(this.mockTeamDTOs.get(2));
        verify(this.userService, never()).getUser(any());
    }

    @Test
    @DisplayName("Should sort by wins if pct are equal")
    void testGetRankingsSortedByWins() {
        this.team1.setWins(80);
        this.team1.setLosses(20);
        this.team1.setPct("0.800");

        this.team2.setWins(90);
        this.team2.setLosses(22);
        this.team2.setPct("0.800");
        this.mockTeamRepositoryAndMapper();

        Map<League, Map<Division, List<TeamDTO>>> standings = this.teamService.getStandings(null);

        this.assertCommonStandingsStructure(standings);

        assertThat(standings.get(League.AL).get(Division.EAST)).containsExactly(this.mockTeamDTOs.get(1), this.mockTeamDTOs.getFirst());
        verify(this.userService, never()).getUser(any());
    }

    private void mockTeamRepositoryAndMapper() {
        when(this.teamRepository.findAll()).thenReturn(List.of(this.team1, this.team2, this.team3));
        when(this.teamMapper.toTeamDTO(this.team1)).thenReturn(this.mockTeamDTOs.getFirst());
        when(this.teamMapper.toTeamDTO(this.team2)).thenReturn(this.mockTeamDTOs.get(1));
        when(this.teamMapper.toTeamDTO(this.team3)).thenReturn(this.mockTeamDTOs.get(2));
    }

    private void assertCommonStandingsStructure(Map<League, Map<Division, List<TeamDTO>>> standings) {
        assertThat(standings).isNotNull().containsKeys(League.AL, League.NL);
        assertThat(standings.get(League.AL)).containsKeys(Division.EAST, Division.CENTRAL, Division.WEST);
        assertThat(standings.get(League.NL)).containsKeys(Division.EAST, Division.CENTRAL, Division.WEST);
    }

    @Test
    @DisplayName("Should return empty standings when no teams exist")
    void testGetEmptyStandings() {
        when(this.teamRepository.findAll()).thenReturn(List.of());

        Map<League, Map<Division, List<TeamDTO>>> standings = this.teamService.getStandings("anyUser");

        assertThat(standings.get(League.AL).values()).allMatch(List::isEmpty);
        assertThat(standings.get(League.NL).values()).allMatch(List::isEmpty);
    }

    @Test
    @DisplayName("Should get the wins of a certain team against certain rivals")
    void testGetWinsPerRivals() {
        Set<String> rivals = Set.of(TEST_TEAM2_NAME, TEST_TEAM3_NAME);
        WinsPerRivalDTO winsAgainstTeam2 = BuildMocksFactory.buildWinsPerRivalDTO(TEST_TEAM2_NAME, 6, 3);
        WinsPerRivalDTO winsAgainstTeam3 = BuildMocksFactory.buildWinsPerRivalDTO(TEST_TEAM3_NAME, 10, 8);
        List<WinsPerRivalDTO> expectedResult = List.of(winsAgainstTeam2, winsAgainstTeam3);

        when(this.matchRepository.findWinsPerRival(TEST_TEAM1_NAME, rivals)).thenReturn(expectedResult);

        List<WinsPerRivalDTO> result = this.teamService.getWinsPerRivals(TEST_TEAM1_NAME, rivals);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(WinsPerRivalDTO::rivalTeamName).containsExactly(TEST_TEAM2_NAME, TEST_TEAM3_NAME);
        assertThat(result).extracting(WinsPerRivalDTO::wins).containsExactly(3L, 8L);
    }

    @Test
    @DisplayName("Should throw InvalidTypeException when no rival teams are provided")
    void testGetWinsPerRivalsWithoutRivals() {
        assertThatThrownBy(() -> this.teamService.getWinsPerRivals(TEST_TEAM1_NAME, Collections.emptySet()))
                .isInstanceOf(InvalidTypeException.class)
                .hasMessage("The rival teams are required");
    }

    @Test
    @DisplayName("Should throw InvalidTypeException when the base team is among the rival ones")
    void testGetWinsPerRivalsWithSameTeam() {
        Set<String> rivals = Set.of(TEST_TEAM2_NAME, TEST_TEAM1_NAME);
        assertThatThrownBy(() -> this.teamService.getWinsPerRivals(TEST_TEAM1_NAME, rivals))
                .isInstanceOf(InvalidTypeException.class)
                .hasMessage("The rival team must differ from the current team");
    }

    @Test
    @DisplayName("Should return the run analytics of the provided team for the current season")
    void testGetRunStatsPerRivals() {
        Set<String> teams = Set.of(TEST_TEAM1_NAME, TEST_TEAM2_NAME);
        RunsStatsDTO statsTeam1 = BuildMocksFactory.buildRunStatsDTO(TEST_TEAM1_NAME, 42, 20);
        RunsStatsDTO statsTeam2 = BuildMocksFactory.buildRunStatsDTO(TEST_TEAM2_NAME, 30, 15);
        List<RunsStatsDTO> expectedResult = List.of(statsTeam1, statsTeam2);

        when(this.teamRepository.findRunsStats(teams)).thenReturn(expectedResult);

        List<RunsStatsDTO> result = this.teamService.getRunStatsPerRival(teams);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RunsStatsDTO::teamName).containsExactly(TEST_TEAM1_NAME, TEST_TEAM2_NAME);
        assertThat(result).extracting(RunsStatsDTO::runsScored).containsExactly(42, 30);
    }

    @Test
    @DisplayName("Should return the win distribution of a certain team")
    void testGetWinDistribution() {
        WinDistributionDTO expectedResult = BuildMocksFactory.buildWinDistributionDTO(TEST_TEAM1_NAME, 30, 25, 20, 10);
        double homePct = (double) 25 / 30;
        double formattedHomePct = Math.round(homePct * 1000.0) / 1000.0;
        double roadPct = (double) 10 / 20;
        double formattedRoadPct = Math.round(roadPct * 1000.0) / 1000.0;

        when(this.teamRepository.findByNameOrThrow(TEST_TEAM1_NAME)).thenReturn(this.team1);
        when(this.teamRepository.findWinDistribution(TEST_TEAM1_NAME)).thenReturn(expectedResult);

        WinDistributionDTO result = this.teamService.getWinDistribution(TEST_TEAM1_NAME);

        assertThat(result.teamName()).isEqualTo(TEST_TEAM1_NAME);
        assertThat(result.getHomeWinPct()).isEqualTo(formattedHomePct);
        assertThat(result.getRoadWinPct()).isEqualTo(formattedRoadPct);
    }

    @Test
    @DisplayName("Should return a map of historic rankings grouped by team name")
    void testGetHistoricRanking() {
        Set<String> teams = Set.of(TEST_TEAM1_NAME);
        LocalDate dateFrom = LocalDate.of(2026, 3, 25);
        DailyStandings standing = new DailyStandings();
        HistoricRankingDTO dto = BuildMocksFactory.buildHistoricRankingDTO(TEST_TEAM1_NAME, dateFrom, 1, 10, 5);
        List<DailyStandings> queryResult = List.of(standing);

        when(this.dailyStandingsRepository.findHistoricRanking(teams, dateFrom)).thenReturn(queryResult);
        when(this.dailyStandingsMapper.toHistoricRankingDTO(standing)).thenReturn(dto);

        Map<String, List<HistoricRankingDTO>> result = this.teamService.getHistoricRanking(teams, dateFrom);

        assertThat(result).isNotNull().containsKey(TEST_TEAM1_NAME);
        assertThat(result.get(TEST_TEAM1_NAME)).hasSize(1);
        assertThat(result.get(TEST_TEAM1_NAME).getFirst().teamName()).isEqualTo(TEST_TEAM1_NAME);
    }

    @Test
    @DisplayName("Should use default date (one month ago) when dateFrom is nor provided")
    void testGetHistoricRankingWithDefaultDate() {
        Set<String> teams = Set.of(TEST_TEAM1_NAME);

        when(this.dailyStandingsRepository.findHistoricRanking(eq(teams), any(LocalDate.class))).thenReturn(List.of());

        this.teamService.getHistoricRanking(teams, null);

        verify(this.dailyStandingsRepository).findHistoricRanking(teams, LocalDate.now().minusMonths(1));
    }

    @Test
    @DisplayName("Should update stats and save both teams when updating ranking")
    void testUpdateRanking() {
        this.team1.setWins(TEST_TEAM1_WINS);
        this.team1.setLosses(TEST_TEAM1_LOSSES);
        this.team2.setWins(TEST_TEAM2_WINS);
        this.team2.setLosses(TEST_TEAM2_LOSSES);

        when(this.teamRepository.findByLeagueAndDivision(any(), any())).thenReturn(Arrays.asList(this.team1, this.team2));

        this.teamService.updateRanking(this.team1, this.team2);

        assertThat(this.team1.getPct()).isEqualTo(".564");
        assertThat(this.team2.getPct()).isEqualTo(".470");
        assertThat(this.team2.getGamesBehind()).isEqualTo(14.0);

        verify(this.teamRepository, times(1)).save(this.team1);
        verify(this.teamRepository, times(1)).save(this.team2);
    }

    @Test
    @DisplayName("Should update city, championship and info when provided")
    void testUpdateTeamBasicFields() {
        when(this.teamRepository.findByNameOrThrow(TEST_TEAM1_NAME)).thenReturn(this.team1);

        UpdateTeamRequest request = new UpdateTeamRequest(
                Optional.of("City1"),
                Optional.of(2025),
                Optional.of("Test Info"),
                Optional.empty()
        );

        this.teamService.updateTeam(TEST_TEAM1_NAME, request);

        assertThat(this.team1.getCity()).isEqualTo("City1");
        assertThat(this.team1.getChampionships()).contains(2025);
        assertThat(this.team1.getGeneralInfo()).isEqualTo("Test Info");

        verify(this.teamRepository).save(this.team1);
    }

    @Test
    @DisplayName("Should update stadium when provided and free")
    void testUpdateTeamWithStadium() {
        Stadium stadium = new Stadium();
        stadium.setName("New Stadium");

        when(this.teamRepository.findByNameOrThrow(TEST_TEAM1_NAME)).thenReturn(this.team1);
        when(this.stadiumRepository.findByNameOrThrow("New Stadium")).thenReturn(stadium);

        UpdateTeamRequest request = new UpdateTeamRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of("New Stadium")
        );

        this.teamService.updateTeam(TEST_TEAM1_NAME, request);

        assertThat(this.team1.getStadium()).isEqualTo(stadium);
        verify(this.teamRepository).save(this.team1);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when stadium already has a team")
    void testUpdateTeamWithOccupiedStadium() {
        Stadium stadium = new Stadium();
        stadium.setName(OCCUPIED_STADIUM);
        stadium.setTeam(new Team());

        when(this.teamRepository.findByNameOrThrow(TEST_TEAM1_NAME)).thenReturn(this.team1);
        when(this.stadiumRepository.findByNameOrThrow(OCCUPIED_STADIUM)).thenReturn(stadium);

        UpdateTeamRequest request = new UpdateTeamRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(OCCUPIED_STADIUM)
        );

        assertThatThrownBy(() -> this.teamService.updateTeam(TEST_TEAM1_NAME, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already has a team");

        verify(this.teamRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw TeamNotFoundException when team does not exist")
    void testUpdateTeamWithUnknownTeam() {
        when(this.teamRepository.findByNameOrThrow(UNKNOWN_TEAM)).thenCallRealMethod();

        UpdateTeamRequest request = new UpdateTeamRequest(
                Optional.of("City"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        assertThatThrownBy(() -> this.teamService.updateTeam(UNKNOWN_TEAM, request))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage("Team Not Found");

        verify(this.teamRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw StadiumNotFoundException when stadium does not exist")
    void testUpdateTeamWithUnknownStadium() {
        when(this.teamRepository.findByNameOrThrow(TEST_TEAM1_NAME)).thenReturn(this.team1);
        when(this.stadiumRepository.findByNameOrThrow(UNKNOWN_STADIUM)).thenCallRealMethod();

        UpdateTeamRequest request = new UpdateTeamRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(UNKNOWN_STADIUM)
        );

        assertThatThrownBy(() -> this.teamService.updateTeam(TEST_TEAM1_NAME, request))
                .isInstanceOf(StadiumNotFoundException.class)
                .hasMessage("Stadium Not Found");

        verify(this.teamRepository, never()).save(any());
    }
}