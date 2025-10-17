package com.mlb.mlbportal.unit;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.mappers.TeamMapper;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.MatchService;
import com.mlb.mlbportal.services.TeamService;
import static com.mlb.mlbportal.utils.TestConstants.*;

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

    private Team team1;
    private Team team2;
    private Team team3;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.team1 = new Team(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM1_WINS, TEST_TEAM1_LOSSES, League.AL,
                Division.EAST, TEST_TEAM1_LOGO);
        this.team2 = new Team(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, TEST_TEAM2_WINS, TEST_TEAM2_LOSSES, League.NL,
                Division.CENTRAL, TEST_TEAM2_LOGO);
        this.team3 = new Team(TEST_TEAM3_NAME, TEST_TEAM3_ABBREVIATION, TEST_TEAM3_WINS, TEST_TEAM3_LOSSES, League.AL,
                Division.WEST, TEST_TEAM3_LOGO);

        this.team1.setTotalGames(TEST_TEAM1_WINS + TEST_TEAM1_LOSSES);
        this.team1.setPct(TEST_TEAM1_WINS / this.team1.getTotalGames());

        this.team2.setTotalGames(TEST_TEAM2_WINS + TEST_TEAM2_LOSSES);
        this.team2.setPct(TEST_TEAM2_WINS / this.team2.getTotalGames());

        this.team3.setTotalGames(TEST_TEAM3_WINS + TEST_TEAM3_LOSSES);
        this.team3.setPct(TEST_TEAM3_WINS / this.team3.getTotalGames());
    }

    private List<TeamDTO> buildMockDTOs() {
        TeamDTO dto1 = new TeamDTO(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM1_LOGO, this.team1.getLeague(), this.team1.getDivision(), this.team1.getTotalGames(), this.team1.getWins(), this.team1.getLosses(), this.team1.getPct(), 1.0, "0-0");
        TeamDTO dto2 = new TeamDTO(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, TEST_TEAM2_LOGO, this.team2.getLeague(), this.team2.getDivision(), this.team2.getTotalGames(), this.team2.getWins(), this.team2.getLosses(), this.team2.getPct(), 0.0, "0-0");
        TeamDTO dto3 = new TeamDTO(TEST_TEAM3_NAME, TEST_TEAM3_ABBREVIATION, TEST_TEAM3_LOGO, this.team3.getLeague(), this.team3.getDivision(), this.team3.getTotalGames(), this.team3.getWins(), this.team3.getLosses(), this.team3.getPct(), 41.0, "0-0");
        return Arrays.asList(dto1, dto2, dto3);
    }

    @Test
    @DisplayName("Should return all teams mapped correctly as DTOs")
    void testGetAllTeams() {
        List<Team> mockTeams = List.of(this.team1, this.team2, this.team3);
        when(this.teamRepository.findAll()).thenReturn(mockTeams);

        List<TeamDTO> mockTeamsDTOs = this.buildMockDTOs();

        when(this.teamMapper.toTeamDTOList(mockTeams)).thenReturn(mockTeamsDTOs);

        List<TeamDTO> result = this.teamService.getTeams();

        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyElementsOf(mockTeamsDTOs);
    }

    @Test
    @DisplayName("Should return standings grouped by league and division with sorted DTOs")
    void testGetStandings() {
        List<Team> mockTeams = List.of(this.team1, this.team2, this.team3);
        when(this.teamRepository.findAll()).thenReturn(mockTeams);

        when(this.teamRepository.findByLeagueAndDivision(League.AL, Division.EAST))
                .thenReturn(Arrays.asList(this.team1));

        when(this.teamRepository.findByLeagueAndDivision(League.NL, Division.CENTRAL))
                .thenReturn(Arrays.asList(this.team2));

        when(this.teamRepository.findByLeagueAndDivision(League.AL, Division.WEST))
                .thenReturn(Arrays.asList(this.team3));

        List<TeamDTO> mockDTOs = this.buildMockDTOs();

        when(this.teamMapper.toTeamDTO(this.team1)).thenReturn(mockDTOs.get(0));
        when(this.teamMapper.toTeamDTO(this.team2)).thenReturn(mockDTOs.get(1));
        when(this.teamMapper.toTeamDTO(this.team3)).thenReturn(mockDTOs.get(2));

        Map<League, Map<Division, List<TeamDTO>>> standings = this.teamService.getStandings();

        assertThat(standings).hasSize(2);
        assertThat(standings.get(League.AL)).hasSize(3);
        assertThat(standings.get(League.NL)).hasSize(3);

        assertThat(standings.get(League.AL).get(Division.EAST)).containsExactly(mockDTOs.get(0));
        assertThat(standings.get(League.NL).get(Division.CENTRAL)).containsExactly(mockDTOs.get(1));
        assertThat(standings.get(League.AL).get(Division.WEST)).containsExactly(mockDTOs.get(2));
    }

    @Test
    @DisplayName("Should return empty standings when no teams exist")
    void testGetEmptyStandings() {
        when(this.teamRepository.findAll()).thenReturn(List.of());
        Map<League, Map<Division, List<TeamDTO>>> standings = this.teamService.getStandings();
        assertThat(standings.get(League.AL).values()).allMatch(List::isEmpty);
        assertThat(standings.get(League.NL).values()).allMatch(List::isEmpty);
    }
}