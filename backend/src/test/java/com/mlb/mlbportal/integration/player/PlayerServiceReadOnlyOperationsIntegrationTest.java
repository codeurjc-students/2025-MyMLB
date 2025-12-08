package com.mlb.mlbportal.integration.player;

import java.util.List;

import static com.mlb.mlbportal.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

import com.mlb.mlbportal.dto.player.PlayerDTO;
import com.mlb.mlbportal.dto.player.position.PositionPlayerDTO;
import com.mlb.mlbportal.dto.player.position.PositionPlayerSummaryDTO;
import com.mlb.mlbportal.services.player.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
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
    private PlayerService playerService;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.playerRepository.deleteAll();
        this.positionPlayerRepository.deleteAll();
        this.pitcherRepository.deleteAll();
        this.teamRepository.deleteAll();

        List<Team> teams = BuildMocksFactory.setUpTeamMocks();
        this.teamRepository.saveAll(teams);

        List<PositionPlayer> positionPlayers = BuildMocksFactory.buildPositionPlayers(teams);
        List<Pitcher> pitchers = BuildMocksFactory.buildPitchers(teams);

        this.positionPlayerRepository.saveAll(positionPlayers);
        this.pitcherRepository.saveAll(pitchers);
    }

    @Test
    @DisplayName("Should return all players")
    void testGetAllPlayers() {
        Page<PlayerDTO> result = this.playerService.getAllPlayers(0, 10);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).extracting(PlayerDTO::name).containsExactlyInAnyOrder(PLAYER1_NAME, PLAYER2_NAME, PLAYER3_NAME);
    }

    @Test
    @DisplayName("Should return all position players")
    void testGetAllPositionPlayers() {
        Page<PositionPlayerDTO> result = this.playerService.getAllPositionPlayers(0, 10);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(PositionPlayerDTO::name).containsExactlyInAnyOrder(PLAYER1_NAME, PLAYER2_NAME);
        assertThat(result.getContent()).extracting(PositionPlayerDTO::teamName).containsExactlyInAnyOrder(TEST_TEAM1_NAME, TEST_TEAM1_NAME);
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