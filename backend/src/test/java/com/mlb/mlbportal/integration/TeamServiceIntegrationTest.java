package com.mlb.mlbportal.integration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_YEAR;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_WINS;
import static org.assertj.core.api.Assertions.assertThat;

import com.mlb.mlbportal.dto.team.HistoricRankingDTO;
import com.mlb.mlbportal.dto.team.RunsStatsDTO;
import com.mlb.mlbportal.dto.team.WinDistributionDTO;
import com.mlb.mlbportal.dto.team.WinsPerRivalDTO;
import com.mlb.mlbportal.models.DailyStandings;
import com.mlb.mlbportal.repositories.DailyStandingsRepository;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.mlb.mlbportal.dto.team.UpdateTeamRequest;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.services.team.TeamService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
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

    @Autowired
    private DailyStandingsRepository dailyStandingsRepository;

    private Team team1;
    private Team team2;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.matchRepository.deleteAll();
        this.teamRepository.deleteAll();
        this.userRepository.deleteAll();
        this.stadiumRepository.deleteAll();

        this.team1 = new Team(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, League.AL, Division.EAST, TEST_TEAM1_WINS, TEST_TEAM1_LOSSES, TEST_TEAM1_CITY);
        this.team2 = new Team(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, League.AL, Division.EAST, TEST_TEAM2_WINS, TEST_TEAM2_LOSSES, TEST_TEAM2_CITY);
        Team team3 = new Team(TEST_TEAM3_NAME, TEST_TEAM3_ABBREVIATION, League.NL, Division.CENTRAL, TEST_TEAM3_WINS, TEST_TEAM3_LOSSES, TEST_TEAM3_CITY);

        this.team1.setRunsScored(5);
        this.team1.setRunsAllowed(3);
        this.team1.setHomeGamesPlayed(0);
        this.team1.setHomeGamesWins(0);
        this.team1.setRoadGamesPlayed(1);
        this.team1.setRoadGamesWins(1);

        this.team2.setRunsScored(3);
        this.team2.setRunsAllowed(5);
        this.team2.setHomeGamesPlayed(1);
        this.team2.setHomeGamesWins(0);
        this.team2.setRoadGamesPlayed(0);
        this.team2.setRoadGamesWins(0);

        this.teamRepository.saveAll(List.of(this.team1, this.team2, team3));

        DailyStandings historicStanding = new DailyStandings(
                this.team1,
                LocalDate.now().minusDays(1),
                1
        );
        this.dailyStandingsRepository.saveAndFlush(historicStanding);

        Team persistedTeam1 = this.teamRepository.findByName(TEST_TEAM1_NAME).orElseThrow();
        Team persistedTeam2 = this.teamRepository.findByName(TEST_TEAM2_NAME).orElseThrow();

        LocalDateTime now = LocalDateTime.now();
        Match newMatch = BuildMocksFactory.buildMatch(persistedTeam1, persistedTeam2, 5, 3, now.minusDays(4), MatchStatus.FINISHED);
        newMatch.setWinnerTeam(persistedTeam1);

        this.matchRepository.save(newMatch);

        // Prepare the PCT for the test Teams
        this.teamService.updateRanking(this.team1, this.team2);
        this.teamService.updateRanking(this.team2, team3);
    }

    @Test
    @DisplayName("Should get the wins of a certain team against certain rivals")
    void testGetWinsPerRivals() {
        Set<String> rivals = Set.of(TEST_TEAM2_NAME);

        List<WinsPerRivalDTO> result = this.teamService.getWinsPerRivals(TEST_TEAM1_NAME, rivals);

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().rivalTeamName()).isEqualTo(TEST_TEAM2_NAME);
        assertThat(result.getFirst().wins()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return the run analytics of the provided team for the current season")
    void testGetRunStatsPerRival() {
        Set<String> teams = Set.of(TEST_TEAM1_NAME, TEST_TEAM2_NAME);

        List<RunsStatsDTO> result = this.teamService.getRunStatsPerRival(teams);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RunsStatsDTO::teamName).containsExactly(TEST_TEAM1_NAME, TEST_TEAM2_NAME);
        assertThat(result).extracting(RunsStatsDTO::runsScored).containsExactly(5, 3);
    }

    @Test
    @DisplayName("Should return the win distribution of a certain team")
    void testGetWinDistribution() {
        WinDistributionDTO result = this.teamService.getWinDistribution(TEST_TEAM1_NAME);

        assertThat(result.teamName()).isEqualTo(TEST_TEAM1_NAME);
        assertThat(result.roadWins()).isEqualTo(1);
        assertThat(result.getHomeWinPct()).isEqualTo(0.0);
        assertThat(result.getRoadWinPct()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should return a map of historic rankings grouped by team name")
    void testGetHistoricRanking() {
        LocalDate today = java.time.LocalDate.now();
        Set<String> teams = Set.of(TEST_TEAM1_NAME);
        Map<String, List<HistoricRankingDTO>> result = this.teamService.getHistoricRanking(teams, today.minusDays(1));

        assertThat(result).containsKey(TEST_TEAM1_NAME);
        assertThat(result.get(TEST_TEAM1_NAME)).isNotEmpty();
        assertThat(result.get(TEST_TEAM1_NAME).getFirst().rank()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should update the ranking of the teams involve in a match")
    void testUpdateRanking() {
        this.teamService.updateRanking(this.team1, this.team2);

        Team storedTeam1 = this.teamRepository.findByNameOrThrow(this.team1.getName());
        Team storedTeam2 = this.teamRepository.findByNameOrThrow(this.team2.getName());

        int totalGames1 = TEST_TEAM1_WINS + TEST_TEAM1_LOSSES;
        assertThat(storedTeam1.getWins()).isEqualTo(TEST_TEAM1_WINS);
        assertThat(storedTeam1.getTotalGames()).isEqualTo(totalGames1);

        assertThat(storedTeam2.getWins()).isEqualTo(TEST_TEAM2_WINS);
        assertThat(storedTeam2.getTotalGames()).isEqualTo(TEST_TEAM2_WINS + TEST_TEAM2_LOSSES);

        assertThat(storedTeam1.getPct()).isEqualTo(".564");
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