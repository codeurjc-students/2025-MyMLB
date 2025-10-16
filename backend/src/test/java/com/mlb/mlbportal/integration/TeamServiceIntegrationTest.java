package com.mlb.mlbportal.integration;

import static com.mlb.mlbportal.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.TeamService;

@SpringBootTest
class TeamServiceIntegrationTest {
    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamService teamService;

    private Team team1, team2, team3;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.teamRepository.deleteAll();
        this.team1 = new Team(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM1_WINS, TEST_TEAM1_LOSSES, League.AL,
                Division.EAST, TEST_TEAM1_LOGO);
        this.team2 = new Team(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, TEST_TEAM2_WINS, TEST_TEAM2_LOSSES, League.AL,
                Division.EAST, TEST_TEAM2_LOGO);
        this.team3 = new Team(TEST_TEAM3_NAME, TEST_TEAM3_ABBREVIATION, TEST_TEAM3_WINS, TEST_TEAM3_LOSSES, League.NL,
                Division.CENTRAL, TEST_TEAM3_LOGO);

        this.teamRepository.saveAll(List.of(this.team1, this.team2, this.team3));
    }

    @Test
    @DisplayName("Should return all teams with calculated stats")
    void testGetAllTeams() {
        List<TeamDTO> result = this.teamService.getTeams();

        assertThat(result).hasSize(3);

        TeamDTO teamDTO1 = result.stream().filter(team -> team.abbreviation().equals(TEST_TEAM1_ABBREVIATION))
                .findFirst().orElseThrow();
        assertThat(teamDTO1.totalGames()).isEqualTo(149);
        assertThat(teamDTO1.pct()).isEqualTo(0.469);
    }

    @Test
    @DisplayName("Should return standings grouped and sorted by pct")
    void testGetStandings() {
        Map<League, Map<Division, List<TeamDTO>>> standings = this.teamService.getStandings();

        assertThat(standings).hasSize(2);
        assertThat(standings.get(League.AL)).hasSize(3);
        assertThat(standings.get(League.NL)).hasSize(3);

        List<TeamDTO> alEast = standings.get(League.AL).get(Division.EAST);
        assertThat(alEast).hasSize(2);

        assertThat(alEast.get(0).abbreviation()).isEqualTo(TEST_TEAM2_ABBREVIATION);
        assertThat(alEast.get(1).abbreviation()).isEqualTo(TEST_TEAM1_ABBREVIATION);

        assertThat(alEast.get(0).gamesBehind()).isEqualTo(0.0);
        assertThat(alEast.get(1).gamesBehind()).isEqualTo(14.0);
    }
}