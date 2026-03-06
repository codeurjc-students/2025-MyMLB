package com.mlb.mlbportal.integration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.mlb.mlbportal.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.dto.team.TeamInfoDTO;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.dto.team.UpdateTeamRequest;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.services.team.TeamService;
import com.mlb.mlbportal.utils.BuildMocksFactory;

@Slf4j
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    private Team team1, team2, team3;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.matchRepository.deleteAll();
        this.teamRepository.deleteAll();
        this.userRepository.deleteAll();
        this.stadiumRepository.deleteAll();

        this.team1 = new Team(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, League.AL, Division.EAST, TEST_TEAM1_LOGO);
        this.team1.setCity(TEST_TEAM1_CITY);
        this.team1.setWins(TEST_TEAM1_WINS);
        this.team1.setLosses(TEST_TEAM1_LOSSES);

        this.team2 = new Team(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, League.AL, Division.EAST, TEST_TEAM2_LOGO);
        this.team2.setWins(TEST_TEAM2_WINS);
        this.team2.setLosses(TEST_TEAM2_LOSSES);
        this.team2.setCity(TEST_TEAM2_CITY);

        this.team3 = new Team(TEST_TEAM3_NAME, TEST_TEAM3_ABBREVIATION, League.NL, Division.CENTRAL, TEST_TEAM3_LOGO);
        this.team3.setWins(TEST_TEAM3_WINS);
        this.team3.setLosses(TEST_TEAM3_LOSSES);
        this.team3.setCity(TEST_TEAM3_CITY);

        this.teamRepository.saveAll(List.of(this.team1, this.team2, this.team3));

        Team persistedTeam1 = this.teamRepository.findByName(TEST_TEAM1_NAME).orElseThrow();
        Team persistedTeam2 = this.teamRepository.findByName(TEST_TEAM2_NAME).orElseThrow();

        LocalDateTime now = LocalDateTime.now();

        this.matchRepository.save(new Match(persistedTeam1, persistedTeam2, 5, 3, now.minusDays(4), MatchStatus.FINISHED));

        // Prepare the PCT for the test Teams
        this.teamService.updateRanking(this.team1, this.team2);
        this.teamService.updateRanking(this.team2, this.team3);
    }

    @Test
    @DisplayName("Should return all teams with calculated stats")
    void testGetAllTeams() {
        Page<TeamInfoDTO> result = this.teamService.getTeams(0, 10);

        assertThat(result.getContent()).hasSize(3);

        TeamInfoDTO teamDTO1 = result.stream().filter(team -> team.teamStats().abbreviation().equals(TEST_TEAM1_ABBREVIATION))
                .findFirst().orElseThrow();
        assertThat(teamDTO1.teamStats().totalGames()).isEqualTo(149);
        assertThat(teamDTO1.teamStats().pct()).isEqualTo(".470");
    }

    @Test
    @DisplayName("Should return all available teams")
    void testGetAvailableTeams() {
        Page<TeamSummary> result = this.teamService.getAvailableTeams(0, 10);

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("Should return standings grouped and ordered correctly when no user favorites")
    void testGetStandingsWithoutUserFavorites() {
        Map<League, Map<Division, List<TeamDTO>>> standings = this.teamService.getStandings(null);

        assertThat(standings).isNotNull().containsKeys(League.AL, League.NL);

        assertThat(standings.get(League.AL)).containsKeys(Division.EAST, Division.CENTRAL, Division.WEST);
        assertThat(standings.get(League.NL)).containsKeys(Division.EAST, Division.CENTRAL, Division.WEST);

        List<TeamDTO> alEast = standings.get(League.AL).get(Division.EAST);
        assertThat(alEast).hasSize(2);

        assertThat(alEast.getFirst().abbreviation()).isEqualTo(TEST_TEAM2_ABBREVIATION);
        assertThat(alEast.get(1).abbreviation()).isEqualTo(TEST_TEAM1_ABBREVIATION);
    }

    @Test
    @DisplayName("Should prioritize divisions based on user's favorite teams")
    void testGetStandingsWithUserFavorites() {
        UserEntity user = BuildMocksFactory.setUpUsers().getFirst();
        user.setFavTeams(Set.of(this.team1, this.team3));
        this.userRepository.save(user);

        Map<League, Map<Division, List<TeamDTO>>> standings = this.teamService.getStandings(USER1_USERNAME);

        assertThat(standings).isNotNull().containsKeys(League.AL, League.NL);

        assertThat(standings.get(League.AL).get(Division.EAST))
                .extracting(TeamDTO::abbreviation)
                .containsExactly(TEST_TEAM2_ABBREVIATION, TEST_TEAM1_ABBREVIATION);

        assertThat(standings.get(League.NL).get(Division.CENTRAL))
                .extracting(TeamDTO::abbreviation)
                .containsExactly(TEST_TEAM3_ABBREVIATION);
    }

    @Test
    @DisplayName("Should update the ranking of the teams involve in a match")
    void testUpdateRanking() {
        this.teamService.updateRanking(this.team1, this.team2);

        Team storedTeam1 = this.teamRepository.findByNameOrThrow(this.team1.getName());
        Team storedTeam2 = this.teamRepository.findByNameOrThrow(this.team2.getName());

        int totalGames1 = TEST_TEAM1_WINS + TEST_TEAM1_LOSSES;
        log.info("TEAM 1 WINS: {} ", storedTeam1.getWins());
        assertThat(storedTeam1.getWins()).isEqualTo(TEST_TEAM1_WINS);
        assertThat(storedTeam1.getTotalGames()).isEqualTo(totalGames1);

        assertThat(storedTeam2.getWins()).isEqualTo(TEST_TEAM2_WINS);
        assertThat(storedTeam2.getTotalGames()).isEqualTo(TEST_TEAM2_WINS + TEST_TEAM2_LOSSES);

        assertThat(storedTeam1.getPct()).isEqualTo(".470");
    }

    @Test
    @DisplayName("Should persist Stadium and associate it with the correct Team")
    void testTeamStadiumPersistence() {
        Team team = this.teamRepository.findByName(TEST_TEAM1_NAME).orElseThrow(TeamNotFoundException::new);
        Stadium stadium = new Stadium(STADIUM1_NAME, STADIUM1_YEAR, team);
        team.setStadium(stadium);

        this.teamRepository.save(team);

        Team persistedTeam = this.teamRepository.findByName(TEST_TEAM1_NAME).orElseThrow(TeamNotFoundException::new);
        Stadium persistedStadium = persistedTeam.getStadium();

        assertThat(persistedStadium).isNotNull();
        assertThat(persistedStadium.getName()).isEqualTo("Stadium1");
        assertThat(persistedStadium.getTeam()).isEqualTo(persistedTeam);
    }

    @Test
    @DisplayName("Should update team city, info and championships")
    void testUpdateTeamBasicFields() {
        this.teamRepository.findByName(TEST_TEAM1_NAME).orElseThrow();

        UpdateTeamRequest request = new UpdateTeamRequest(
                Optional.of("Updated City"),
                Optional.of(28),
                Optional.of("Updated Info"),
                Optional.empty()
        );

        this.teamService.updateTeam(TEST_TEAM1_NAME, request);

        Team updatedTeam = this.teamRepository.findByName("Updated City Team1").orElseThrow();
        assertThat(updatedTeam.getCity()).isEqualTo("Updated City");
        assertThat(updatedTeam.getChampionships()).contains(28);
        assertThat(updatedTeam.getGeneralInfo()).isEqualTo("Updated Info");
    }

    @Test
    @DisplayName("Should update stadium when provided and free")
    void testUpdateTeamWithStadium() {
        Team team = this.teamRepository.findByName(TEST_TEAM1_NAME).orElseThrow();
        Stadium stadium = new Stadium(STADIUM1_NAME, STADIUM1_YEAR, null);
        this.stadiumRepository.save(stadium);
        this.teamRepository.save(team);

        UpdateTeamRequest request = new UpdateTeamRequest(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(STADIUM1_NAME)
        );

        this.teamService.updateTeam(TEST_TEAM1_NAME, request);

        Team updatedTeam = this.teamRepository.findByName(TEST_TEAM1_NAME).orElseThrow();
        assertThat(updatedTeam.getStadium()).isNotNull();
        assertThat(updatedTeam.getStadium().getName()).isEqualTo(STADIUM1_NAME);
    }
}