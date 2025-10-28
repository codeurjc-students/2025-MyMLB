package com.mlb.mlbportal.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.dto.team.TeamInfoDTO;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.mappers.TeamMapper;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.MatchService;
import com.mlb.mlbportal.services.TeamService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    @SuppressWarnings("unused")
    private MatchService matchService;

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
        when(teamRepository.findAll()).thenReturn(mockTeams);
        when(teamMapper.toTeamInfoDTOList(mockTeams)).thenReturn(mockTeamInfoDTOs);

        List<TeamInfoDTO> result = teamService.getTeams();

        assertThat(result)
                .hasSize(3)
                .containsExactlyElementsOf(mockTeamInfoDTOs);
    }

    @Test
    @DisplayName("Should return standings grouped by league and division with sorted DTOs")
    void testGetStandings() {
        List<Team> mockTeams = new ArrayList<>(List.of(team1, team2, team3));
        when(teamRepository.findAll()).thenReturn(mockTeams);

        when(teamRepository.findByLeagueAndDivision(League.AL, Division.EAST))
                .thenReturn(new ArrayList<>(List.of(team1)));
        when(teamRepository.findByLeagueAndDivision(League.NL, Division.CENTRAL))
                .thenReturn(new ArrayList<>(List.of(team2)));
        when(teamRepository.findByLeagueAndDivision(League.AL, Division.WEST))
                .thenReturn(new ArrayList<>(List.of(team3)));

        when(teamMapper.toTeamDTO(team1)).thenReturn(mockTeamDTOs.get(0));
        when(teamMapper.toTeamDTO(team2)).thenReturn(mockTeamDTOs.get(1));
        when(teamMapper.toTeamDTO(team3)).thenReturn(mockTeamDTOs.get(2));

        Map<League, Map<Division, List<TeamDTO>>> standings = teamService.getStandings();

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
        when(teamRepository.findAll()).thenReturn(List.of());

        Map<League, Map<Division, List<TeamDTO>>> standings = teamService.getStandings();

        assertThat(standings.get(League.AL).values()).allMatch(List::isEmpty);
        assertThat(standings.get(League.NL).values()).allMatch(List::isEmpty);
    }

    @Test
    @DisplayName("Should return the general info of a team")
    void testGetTeamInfo() {
        when(teamRepository.findByName(TEST_TEAM1_NAME)).thenReturn(Optional.of(team1));
        TeamInfoDTO expected = mockTeamInfoDTOs.get(0);
        when(teamMapper.toTeamInfoDTO(team1)).thenReturn(expected);

        TeamInfoDTO result = teamService.getTeamInfo(TEST_TEAM1_NAME);

        assertThat(result.teamDTO().name()).isEqualTo(expected.teamDTO().name());
        assertThat(result.teamDTO().abbreviation()).isEqualTo(expected.teamDTO().abbreviation());
        assertThat(result.city()).isEqualTo(expected.city());
        assertThat(result.stadium().name()).isEqualTo(expected.stadium().name());
    }

    @Test
    @DisplayName("Should throw TeamNotFoundException for a non existent team")
    void testGetNonExistentTeamInfo() {
        when(teamRepository.findByName(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getTeamInfo("Unknown Team"))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessageContaining("Team Not Found");
    }
}