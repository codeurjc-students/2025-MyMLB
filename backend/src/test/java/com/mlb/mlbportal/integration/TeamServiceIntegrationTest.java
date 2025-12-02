package com.mlb.mlbportal.integration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

import com.mlb.mlbportal.dto.team.TeamInfoDTO;
import com.mlb.mlbportal.dto.team.TeamSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.mlb.mlbportal.dto.team.TeamDTO;
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
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_YEAR;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_LOGO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_LOGO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_LOGO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_WINS;
import static com.mlb.mlbportal.utils.TestConstants.USER1_USERNAME;

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

        this.team1 = new Team(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM1_WINS, TEST_TEAM1_LOSSES, League.AL,
                Division.EAST, TEST_TEAM1_LOGO);
        team1.setCity(TEST_TEAM1_CITY);
        this.team2 = new Team(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, TEST_TEAM2_WINS, TEST_TEAM2_LOSSES, League.AL,
                Division.EAST, TEST_TEAM2_LOGO);
        team2.setCity(TEST_TEAM2_CITY);
        this.team3 = new Team(TEST_TEAM3_NAME, TEST_TEAM3_ABBREVIATION, TEST_TEAM3_WINS, TEST_TEAM3_LOSSES, League.NL,
                Division.CENTRAL, TEST_TEAM3_LOGO);
        team3.setCity(TEST_TEAM3_CITY);

        this.teamRepository.saveAll(List.of(this.team1, this.team2, this.team3));

        Team persistedTeam1 = this.teamRepository.findByName(TEST_TEAM1_NAME).orElseThrow();
        Team persistedTeam2 = this.teamRepository.findByName(TEST_TEAM2_NAME).orElseThrow();

        LocalDateTime now = LocalDateTime.now();

        this.matchRepository.save(new Match(persistedTeam1, persistedTeam2, 5, 3, now.minusDays(4), MatchStatus.FINISHED));
    }

    @Test
    @DisplayName("Should return all teams with calculated stats")
    void testGetAllTeams() {
        List<TeamInfoDTO> result = this.teamService.getTeams();

        assertThat(result).hasSize(3);

        TeamInfoDTO teamDTO1 = result.stream().filter(team -> team.teamStats().abbreviation().equals(TEST_TEAM1_ABBREVIATION))
                .findFirst().orElseThrow();
        assertThat(teamDTO1.teamStats().totalGames()).isEqualTo(149);
        assertThat(teamDTO1.teamStats().pct()).isEqualTo(0.469);
    }

    @Test
    @DisplayName("Should return all available teams")
    void testGetAvailableTeams() {
        Page<TeamSummary> result = this.teamService.getAvailableTeams(0, 10);

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent().size()).isEqualTo(3);
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

        assertThat(alEast.get(0).abbreviation()).isEqualTo(TEST_TEAM2_ABBREVIATION);
        assertThat(alEast.get(1).abbreviation()).isEqualTo(TEST_TEAM1_ABBREVIATION);
    }

    @Test
    @DisplayName("Should prioritize divisions based on user's favorite teams")
    void testGetStandingsWithUserFavorites() {
        UserEntity user = BuildMocksFactory.setUpUsers().getFirst();
        user.setFavTeams(Set.of(team1, team3));
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