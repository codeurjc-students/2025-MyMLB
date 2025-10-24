package com.mlb.mlbportal.integration;

import static com.mlb.mlbportal.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.TeamService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TeamServiceIntegrationTest {
    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamService teamService;

    @Autowired
    private MatchRepository matchRepository;

    private Team team1, team2, team3;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.matchRepository.deleteAll();
        this.teamRepository.deleteAll();

        this.team1 = new Team(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM1_WINS, TEST_TEAM1_LOSSES, League.AL,
                Division.EAST, TEST_TEAM1_LOGO);
        this.team2 = new Team(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, TEST_TEAM2_WINS, TEST_TEAM2_LOSSES, League.AL,
                Division.EAST, TEST_TEAM2_LOGO);
        this.team3 = new Team(TEST_TEAM3_NAME, TEST_TEAM3_ABBREVIATION, TEST_TEAM3_WINS, TEST_TEAM3_LOSSES, League.NL,
                Division.CENTRAL, TEST_TEAM3_LOGO);

        this.teamRepository.saveAll(List.of(this.team1, this.team2, this.team3));

        Team persistedTeam1 = this.teamRepository.findByName(TEST_TEAM1_NAME).orElseThrow();
        Team persistedTeam2 = this.teamRepository.findByName(TEST_TEAM2_NAME).orElseThrow();

        LocalDateTime now = LocalDateTime.now();

        this.matchRepository.save(new Match(persistedTeam1, persistedTeam2, 5, 3, now.minusDays(4), MatchStatus.FINISHED));
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

    @Test
    @DisplayName("Should handle empty divisions accordingly")
    void testEmptyDivisions() {
        this.matchRepository.deleteAll();
        this.teamRepository.deleteAll(this.teamRepository.findAll());

        Map<League, Map<Division, List<TeamDTO>>> standings = this.teamService.getStandings();
        Map<Division, List<TeamDTO>> nlDivisions = standings.get(League.NL);
        Map<Division, List<TeamDTO>> alDivisions = standings.get(League.AL);

        assertThat(nlDivisions).isNotNull();
        assertThat(nlDivisions.values()).allSatisfy(list -> assertThat(list).isEmpty());

        assertThat(alDivisions).isNotNull();
        assertThat(alDivisions.values()).allSatisfy(list -> assertThat(list).isEmpty());
    }
}