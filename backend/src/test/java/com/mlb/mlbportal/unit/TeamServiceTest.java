package com.mlb.mlbportal.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mlb.mlbportal.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.dto.team.TeamInfoDTO;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.mappers.TeamMapper;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.MatchService;
import com.mlb.mlbportal.services.player.PlayerService;
import com.mlb.mlbportal.services.team.TeamService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_USERNAME;
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

        assertThat(result)
                .hasSize(3)
                .containsExactlyElementsOf(this.mockTeamInfoDTOs);
    }

    @Test
    @DisplayName("Should return standings grouped by league and division with sorted DTOs")
    void testGetStandings() {
        List<Team> mockTeams = new ArrayList<>(List.of(team1, team2, team3));
        when(this.teamRepository.findAll()).thenReturn(mockTeams);

        when(this.teamRepository.findByLeagueAndDivision(League.AL, Division.EAST))
                .thenReturn(new ArrayList<>(List.of(team1)));
        when(this.teamRepository.findByLeagueAndDivision(League.NL, Division.CENTRAL))
                .thenReturn(new ArrayList<>(List.of(team2)));
        when(this.teamRepository.findByLeagueAndDivision(League.AL, Division.WEST))
                .thenReturn(new ArrayList<>(List.of(team3)));

        when(this.teamMapper.toTeamDTO(team1)).thenReturn(this.mockTeamDTOs.get(0));
        when(this.teamMapper.toTeamDTO(team2)).thenReturn(this.mockTeamDTOs.get(1));
        when(this.teamMapper.toTeamDTO(team3)).thenReturn(this.mockTeamDTOs.get(2));

        Map<League, Map<Division, List<TeamDTO>>> standings = this.teamService.getStandings(TEST_USER_USERNAME);

        assertThat(standings).hasSize(2);
        assertThat(standings.get(League.AL)).hasSize(3);
        assertThat(standings.get(League.NL)).hasSize(3);

        assertThat(standings.get(League.AL).get(Division.EAST))
                .containsExactly(mockTeamDTOs.get(0));
        assertThat(standings.get(League.NL).get(Division.CENTRAL))
                .containsExactly(mockTeamDTOs.get(1));
        assertThat(standings.get(League.AL).get(Division.WEST))
                .containsExactly(mockTeamDTOs.get(2));
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
}