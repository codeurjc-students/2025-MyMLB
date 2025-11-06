package com.mlb.mlbportal.integration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.dto.player.PitcherDTO;
import com.mlb.mlbportal.dto.player.PitcherSummaryDTO;
import com.mlb.mlbportal.dto.player.PlayerDTO;
import com.mlb.mlbportal.dto.player.PositionPlayerDTO;
import com.mlb.mlbportal.dto.player.PositionPlayerSummaryDTO;
import com.mlb.mlbportal.handler.notFound.PlayerNotFoundException;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.PitcherPositions;
import com.mlb.mlbportal.models.enums.PlayerPositions;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.player.PitcherRepository;
import com.mlb.mlbportal.repositories.player.PlayerRepository;
import com.mlb.mlbportal.repositories.player.PositionPlayerRepository;
import com.mlb.mlbportal.services.player.PlayerService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_PLAYER;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlayerServiceIntegrationTest {
    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PositionPlayerRepository positionPlayerRepository;

    @Autowired
    private PitcherRepository pitcherRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerService playerService;

    private List<PositionPlayer> positionPlayers;
    private List<Pitcher> pitchers;
    private List<Team> teams;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.playerRepository.deleteAll();
        this.positionPlayerRepository.deleteAll();
        this.pitcherRepository.deleteAll();
        this.teamRepository.deleteAll();

        teams = BuildMocksFactory.setUpTeamMocks();
        this.teamRepository.saveAll(teams);

        this.positionPlayers = BuildMocksFactory.buildPositionPlayers(teams);
        this.pitchers = BuildMocksFactory.buildPitchers(teams);

        this.positionPlayerRepository.saveAll(this.positionPlayers);
        this.pitcherRepository.saveAll(this.pitchers);
    }

    @Test
    @DisplayName("Should return all players")
    void testGetAllPlayers() {
        List<PlayerDTO> result = this.playerService.getAllPlayers();

        assertThat(result).hasSize(3);
        assertThat(result).extracting(PlayerDTO::name)
            .containsExactlyInAnyOrder(PLAYER1_NAME, PLAYER2_NAME, PLAYER3_NAME);
    }

    @Test
    @DisplayName("Should return all position players")
    void testGetAllPositionPlayers() {
        List<PositionPlayerDTO> result = this.playerService.getAllPositionPlayers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(PositionPlayerDTO::name)
            .containsExactlyInAnyOrder(PLAYER1_NAME, PLAYER2_NAME);

        assertThat(result).extracting(PositionPlayerDTO::position)
            .containsExactlyInAnyOrder(PlayerPositions.CF, PlayerPositions.SS);

        assertThat(result).extracting(PositionPlayerDTO::teamName)
            .containsExactlyInAnyOrder(TEST_TEAM1_NAME, TEST_TEAM1_NAME);
    }

    @Test
    @DisplayName("Should return all pitchers")
    void testGetAllPitchers() {
        List<PitcherDTO> result = this.playerService.getAllPitchers();

        assertThat(result).hasSize(1);
        assertThat(result).extracting(PitcherDTO::name)
            .containsOnly(PLAYER3_NAME);

        assertThat(result).extracting(PitcherDTO::position)
            .containsOnly(PitcherPositions.SP);

        assertThat(result).extracting(PitcherDTO::teamName)
            .containsOnly(TEST_TEAM2_NAME);
    }

    @Test
    @DisplayName("Should return the position player by its name")
    void testFindPositionPlayerByName() {
        assertThatNoException().isThrownBy(() -> this.playerService.findPlayerByName(PLAYER1_NAME));
        PlayerDTO result = this.playerService.findPlayerByName(PLAYER1_NAME);

        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo("PositionPlayer");
        assertThat(result.name()).isEqualTo(PLAYER1_NAME);
    }

    @Test
    @DisplayName("Should return the pitcher by its name")
    void testFindPitcherByName() {
        assertThatNoException().isThrownBy(() -> this.playerService.findPlayerByName(PLAYER3_NAME));
        PlayerDTO result = this.playerService.findPlayerByName(PLAYER3_NAME);

        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo("Pitcher");
        assertThat(result.name()).isEqualTo(PLAYER3_NAME);
    }

    @Test
    @DisplayName("Should throw PlayerNotFoundException for an unknown player")
    void testFindUnknownPlayerByName() {
        assertThatThrownBy(() -> this.playerService.findPlayerByName(UNKNOWN_PLAYER))
            .isInstanceOf(PlayerNotFoundException.class)
            .hasMessageContaining("Player Not Found");
    }

    @Test
    @DisplayName("Should return updated position players of a team sorted by name")
    void testGetUpdatedPositionPlayersOfTeam() {
        List<PositionPlayer> result = this.playerService.getUpdatedPositionPlayersOfTeam(this.teams.get(0));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(PositionPlayer::getName)
                .containsExactly(PLAYER1_NAME, PLAYER2_NAME); // sorted
        assertThat(result.get(0).getTeam().getName()).isEqualTo(TEST_TEAM1_NAME);
    }

    @Test
    @DisplayName("Should return updated pitchers of a team sorted by name")
    void testGetUpdatedPitchersOfTeam() {
        List<Pitcher> result = this.playerService.getUpdatedPitchersOfTeam(this.teams.get(1));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo(PLAYER3_NAME);
        assertThat(result.get(0).getTeam().getName()).isEqualTo(TEST_TEAM2_NAME);
    }

    @Test
    @DisplayName("Should return paginated position players of a team")
    void testGetPositionPlayerOfATeam() {
        Page<PositionPlayerSummaryDTO> result = this.playerService.getAllPositionPlayersOfATeam(TEST_TEAM1_NAME, 0, 10);
        List<PositionPlayerSummaryDTO> resultContent = result.getContent();

        assertThat(resultContent).hasSize(2);
        assertThat(resultContent.get(0).name()).isEqualTo(PLAYER1_NAME);
        assertThat(resultContent.get(1).name()).isEqualTo(PLAYER2_NAME);
        
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isZero(); // page index
        assertThat(result.getSize()).isEqualTo(10);  // requested size
        assertThat(result.hasNext()).isFalse();
        assertThat(result.hasPrevious()).isFalse();
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    @DisplayName("Should return paginated pitchers of a team")
    void testGetPitchersOfATeam() {
        Page<PitcherSummaryDTO> result = this.playerService.getAllPitchersOfATeam(TEST_TEAM2_NAME, 0, 10);
        List<PitcherSummaryDTO> resultContent = result.getContent();

        assertThat(resultContent).hasSize(1);
        assertThat(resultContent.get(0).name()).isEqualTo(PLAYER3_NAME);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isZero(); // page index
        assertThat(result.getSize()).isEqualTo(10);  // requested size
        assertThat(result.hasNext()).isFalse();
        assertThat(result.hasPrevious()).isFalse();
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
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
}