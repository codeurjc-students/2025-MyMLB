package com.mlb.mlbportal.unit.team;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mlb.mlbportal.dto.team.TeamSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.dto.team.TeamInfoDTO;
import com.mlb.mlbportal.dto.team.UpdateTeamRequest;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.mappers.TeamMapper;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.MatchService;
import com.mlb.mlbportal.services.UserService;
import com.mlb.mlbportal.services.player.PlayerService;
import com.mlb.mlbportal.services.team.TeamService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import org.springframework.data.domain.Page;

import static com.mlb.mlbportal.utils.TestConstants.OCCUPIED_STADIUM;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_USERNAME;
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_STADIUM;
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_TEAM;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    @SuppressWarnings("unused")
    private MatchService matchService;

    @Mock
    private PlayerService playerService;

    @Mock
    private UserService userService;

    @Mock
    private StadiumRepository stadiumRepository;

    @InjectMocks
    private TeamService teamService;

    private Team team1, team2, team3;
    private List<TeamDTO> mockTeamDTOs;
    private List<TeamInfoDTO> mockTeamInfoDTOs;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        List<Team> teams = BuildMocksFactory.setUpTeamMocks();
        this.team1 = teams.get(0);
        this.team2 = teams.get(1);
        this.team3 = teams.get(2);

        this.mockTeamDTOs = BuildMocksFactory.buildTeamDTOMocks(teams);
        this.mockTeamInfoDTOs = BuildMocksFactory.buildTeamInfoDTOMocks(teams);
    }

    @Test
    @DisplayName("Should return all teams mapped correctly as DTOs")
    void testGetAllTeams() {
        List<Team> mockTeams = List.of(team1, team2, team3);
        when(this.teamRepository.findAll()).thenReturn(mockTeams);
        when(this.teamMapper.toTeamInfoDTOList(mockTeams)).thenReturn(this.mockTeamInfoDTOs);

        List<TeamInfoDTO> result = this.teamService.getTeams();

        assertThat(result).hasSize(3).containsExactlyElementsOf(this.mockTeamInfoDTOs);
    }

    @Test
    @DisplayName("Should return all available teams paginated and sorted")
    void testGetAvailableTeams() {
        List<Team> mockTeams = Arrays.asList(team1, team2, team3);
        List<TeamSummary> teamSummaries = BuildMocksFactory.buildTeamSummaryMocks(mockTeams);
        when(teamRepository.findAvailableTeams()).thenReturn(mockTeams);
        when(teamMapper.toTeamSummaryDTO(team1)).thenReturn(teamSummaries.getFirst());
        when(teamMapper.toTeamSummaryDTO(team2)).thenReturn(teamSummaries.get(1));
        when(teamMapper.toTeamSummaryDTO(team3)).thenReturn(teamSummaries.getLast());

        Page<TeamSummary> result = this.teamService.getAvailableTeams(0, 10);

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return the team")
    void testGetTeam() {
        when(this.teamRepository.findByName(TEST_TEAM1_NAME)).thenReturn(Optional.of(this.team1));
        assertThatNoException().isThrownBy(() -> this.teamService.getTeam(TEST_TEAM1_NAME));

        Team result = this.teamService.getTeam(TEST_TEAM1_NAME);
        assertThat(result.getName()).isEqualTo(TEST_TEAM1_NAME);
        assertThat(result.getAbbreviation()).isEqualTo(this.team1.getAbbreviation());
    }

    @Test
    @DisplayName("Should throw TeamNotFound for an unknown player")
    void testGetUnknownTeam() {
        when(this.teamRepository.findByName(UNKNOWN_TEAM)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.teamService.getTeam(UNKNOWN_TEAM))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessageContaining("Team Not Found");
    }

    @Test
    @DisplayName("Should return standings grouped and ordered prioritizing user's favorite divisions")
    void testGetStandingsWithUserFavorites() {
        this.mockTeamRepositoryAndMapper();

        UserEntity mockUser = BuildMocksFactory.setUpUsers().getLast();
        mockUser.setFavTeams(new HashSet<>(List.of(team1, team3)));
        when(this.userService.getUser(TEST_USER_USERNAME)).thenReturn(mockUser);

        Map<League, Map<Division, List<TeamDTO>>> standings = this.teamService.getStandings(TEST_USER_USERNAME);

        this.assertCommonStandingsStructure(standings);

        assertThat(standings.get(team1.getLeague()).get(team1.getDivision()))
                .containsExactly(this.mockTeamDTOs.get(0));
        assertThat(standings.get(team2.getLeague()).get(team2.getDivision()))
                .containsExactly(this.mockTeamDTOs.get(1));
        assertThat(standings.get(team3.getLeague()).get(team3.getDivision()))
                .containsExactly(this.mockTeamDTOs.get(2));

        verify(this.userService).getUser(TEST_USER_USERNAME);
    }

    @Test
    @DisplayName("Should return standings grouped and ordered correctly when no user is provided")
    void testGetStandingsWithoutUserFavorites() {
        this.mockTeamRepositoryAndMapper();

        Map<League, Map<Division, List<TeamDTO>>> standings = this.teamService.getStandings(null);

        this.assertCommonStandingsStructure(standings);

        assertThat(standings.get(team1.getLeague()).get(team1.getDivision()))
                .containsExactly(this.mockTeamDTOs.get(0));
        assertThat(standings.get(team2.getLeague()).get(team2.getDivision()))
                .containsExactly(this.mockTeamDTOs.get(1));
        assertThat(standings.get(team3.getLeague()).get(team3.getDivision()))
                .containsExactly(this.mockTeamDTOs.get(2));

        verify(this.userService, never()).getUser(any());
    }

    private void mockTeamRepositoryAndMapper() {
        when(this.teamRepository.findAll()).thenReturn(List.of(team1, team2, team3));
        when(this.teamMapper.toTeamDTO(team1)).thenReturn(this.mockTeamDTOs.get(0));
        when(this.teamMapper.toTeamDTO(team2)).thenReturn(this.mockTeamDTOs.get(1));
        when(this.teamMapper.toTeamDTO(team3)).thenReturn(this.mockTeamDTOs.get(2));
    }

    private void assertCommonStandingsStructure(Map<League, Map<Division, List<TeamDTO>>> standings) {
        assertThat(standings).isNotNull()
                .containsKeys(League.AL, League.NL);

        assertThat(standings.get(League.AL))
                .containsKeys(Division.EAST, Division.CENTRAL, Division.WEST);

        assertThat(standings.get(League.NL))
                .containsKeys(Division.EAST, Division.CENTRAL, Division.WEST);
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
    @DisplayName("Should return the general info of a team with updated players and pitchers")
    void testGetTeamInfo() {
        when(this.teamRepository.findByName(TEST_TEAM1_NAME)).thenReturn(Optional.of(team1));

        List<PositionPlayer> mockPositionPlayers = BuildMocksFactory.buildPositionPlayers(List.of(team1, team2, team3));
        List<Pitcher> mockPitchers = BuildMocksFactory.buildPitchers(List.of(team1, team2, team3));

        when(this.playerService.getUpdatedPositionPlayersOfTeam(team1)).thenReturn(mockPositionPlayers);
        when(this.playerService.getUpdatedPitchersOfTeam(team1)).thenReturn(mockPitchers);

        TeamInfoDTO expected = this.mockTeamInfoDTOs.get(0);
        when(this.teamMapper.toTeamInfoDTO(team1)).thenReturn(expected);

        TeamInfoDTO result = this.teamService.getTeamInfo(TEST_TEAM1_NAME);

        assertThat(result).isNotNull();
        assertThat(result.teamStats().name()).isEqualTo(expected.teamStats().name());
        assertThat(result.teamStats().abbreviation()).isEqualTo(expected.teamStats().abbreviation());
        assertThat(result.city()).isEqualTo(expected.city());
        assertThat(result.stadium().name()).isEqualTo(expected.stadium().name());

        assertThat(team1.getPositionPlayers()).isNotEmpty();
        assertThat(team1.getPitchers()).isNotEmpty();

        verify(this.teamRepository).findByName(TEST_TEAM1_NAME);
    }

    @Test
    @DisplayName("Should throw TeamNotFoundException for a non existent team")
    void testGetNonExistentTeamInfo() {
        when(this.teamRepository.findByName(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.teamService.getTeamInfo(UNKNOWN_TEAM))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessageContaining("Team Not Found");
    }

    @Test
    @DisplayName("Should update city, championship and info when provided")
    void testUpdateTeamBasicFields() {
        when(this.teamRepository.findByName(TEST_TEAM1_NAME)).thenReturn(Optional.of(this.team1));

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

        when(this.teamRepository.findByName(TEST_TEAM1_NAME)).thenReturn(Optional.of(this.team1));
        when(this.stadiumRepository.findByName("New Stadium")).thenReturn(Optional.of(stadium));

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

        when(this.teamRepository.findByName(TEST_TEAM1_NAME)).thenReturn(Optional.of(this.team1));
        when(this.stadiumRepository.findByName(OCCUPIED_STADIUM)).thenReturn(Optional.of(stadium));

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
        when(this.teamRepository.findByName(UNKNOWN_TEAM)).thenReturn(Optional.empty());

        UpdateTeamRequest request = new UpdateTeamRequest(
                Optional.of("City"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        assertThatThrownBy(() -> this.teamService.updateTeam(UNKNOWN_TEAM, request))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessageContaining("Team Not Found");

        verify(this.teamRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw StadiumNotFoundException when stadium does not exist")
    void testUpdateTeamWithUnknownStadium() {
        when(this.teamRepository.findByName(TEST_TEAM1_NAME)).thenReturn(Optional.of(this.team1));
        when(this.stadiumRepository.findByName(UNKNOWN_STADIUM)).thenReturn(Optional.empty());

        UpdateTeamRequest request = new UpdateTeamRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(UNKNOWN_STADIUM)
        );

        assertThatThrownBy(() -> this.teamService.updateTeam(TEST_TEAM1_NAME, request))
                .isInstanceOf(com.mlb.mlbportal.handler.notFound.StadiumNotFoundException.class);

        verify(this.teamRepository, never()).save(any());
    }
}