package com.mlb.mlbportal.integration.player;

import java.util.List;
import java.util.stream.Stream;

import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import com.mlb.mlbportal.dto.player.PlayerDTO;
import com.mlb.mlbportal.dto.player.PlayerRankingsDTO;
import com.mlb.mlbportal.dto.player.pitcher.PitcherDTO;
import com.mlb.mlbportal.dto.player.position.PositionPlayerDTO;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.services.player.PlayerService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.handler.notFound.PlayerNotFoundException;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.player.PitcherRepository;
import com.mlb.mlbportal.repositories.player.PlayerRepository;
import com.mlb.mlbportal.repositories.player.PositionPlayerRepository;
import com.mlb.mlbportal.utils.BuildMocksFactory;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class PlayerServiceReadOnlyOperationsIntegrationTest {
    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PositionPlayerRepository positionPlayerRepository;

    @Autowired
    private PitcherRepository pitcherRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlayerService playerService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.playerRepository.deleteAll();
        this.positionPlayerRepository.deleteAll();
        this.pitcherRepository.deleteAll();
        this.teamRepository.deleteAll();

        List<Team> teams = BuildMocksFactory.setUpTeamMocks();
        teams.forEach(team -> team.setTotalGames(10));
        this.teamRepository.saveAllAndFlush(teams);

        List<PositionPlayer> positionPlayers = BuildMocksFactory.buildPositionPlayers(teams);
        List<Pitcher> pitchers = BuildMocksFactory.buildPitchers(teams);

        this.positionPlayerRepository.saveAllAndFlush(positionPlayers);
        this.pitcherRepository.saveAll(pitchers);
        this.playerRepository.flush();
        this.entityManager.clear();
    }

    @Test
    @DisplayName("Should return all players from database and return the correct DTO")
    void testGetAllPlayers() {
        Page<PlayerDTO> result = this.playerService.getAllPlayers(null, null, null, 0, 10);

        assertThat(result.getContent()).hasSize(3);

        long pitchersCount = result.getContent().stream()
                .filter(p -> p instanceof PitcherDTO)
                .count();

        long positionPlayersCount = result.getContent().stream()
                .filter(p -> p instanceof PositionPlayerDTO)
                .count();

        assertThat(pitchersCount).isEqualTo(1);
        assertThat(positionPlayersCount).isEqualTo(2);

        assertThat(result.getContent())
                .extracting(PlayerDTO::name)
                .containsExactlyInAnyOrder(PLAYER1_NAME, PLAYER2_NAME, PLAYER3_NAME);
    }

    @Test
    @DisplayName("Should persist PositionPlayer with its associated Team")
    void testPositionPlayerEntityPersistency() {
        PositionPlayer player = this.positionPlayerRepository.findByName(PLAYER1_NAME).orElseThrow(PlayerNotFoundException::new);
        Team team = player.getTeam();

        assertThat(team).isNotNull();
        assertThat(team.getPositionPlayers()).contains(player);
    }

    @Test
    @DisplayName("Should persist Pitcher with its associated Team")
    void testPitcherEntityPersistency() {
        Pitcher player = this.pitcherRepository.findByName(PLAYER3_NAME).orElseThrow(PlayerNotFoundException::new);
        Team team = player.getTeam();

        assertThat(team).isNotNull();
        assertThat(team.getPitchers()).contains(player);
    }

    @ParameterizedTest(name = "{index} => Filtering by {0}")
    @MethodSource("provideFilterArguments")
    @DisplayName("Should return top players ranking with various active filters")
    void testGetTopPlayerRankingsWithFilters(String filterType, List<String> teams, League league, Division division) {
        List<PlayerRankingsDTO> result = this.playerService
                .getTopPlayersRanking(0, 20, "position", "average", teams, league, division)
                .getContent();

        assertThat(result)
                .hasSize(2)
                .extracting(PlayerRankingsDTO::name)
                .containsExactly(PLAYER1_NAME, PLAYER2_NAME);
    }

    private static Stream<Arguments> provideFilterArguments() {
        return Stream.of(
                Arguments.of("No Filters", null, null, null),
                Arguments.of("Team Filter", List.of(TEST_TEAM1_NAME), null, null),
                Arguments.of("League Filter", null, League.AL, null),
                Arguments.of("Division Filter", null, null, Division.EAST)
        );
    }
}