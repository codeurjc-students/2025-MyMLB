package com.mlb.mlbportal.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.dto.player.PitcherDTO;
import com.mlb.mlbportal.dto.player.PlayerDTO;
import com.mlb.mlbportal.dto.player.PositionPlayerDTO;
import com.mlb.mlbportal.handler.notFound.PlayerNotFoundException;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.PlayerPositions;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.player.PitcherRepository;
import com.mlb.mlbportal.repositories.player.PlayerRepository;
import com.mlb.mlbportal.repositories.player.PositionPlayerRepository;
import com.mlb.mlbportal.services.player.PlayerService;
import com.mlb.mlbportal.utils.BuildMocksFactory;

import static com.mlb.mlbportal.utils.TestConstants.*;

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
    private List<PositionPlayerDTO> positionPlayerDTOs;
    private List<PitcherDTO> pitcherDTOs;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.playerRepository.deleteAll();
        this.positionPlayerRepository.deleteAll();
        this.pitcherRepository.deleteAll();
        this.teamRepository.deleteAll();

        List<Team> teams = BuildMocksFactory.setUpTeamMocks();
        this.teamRepository.saveAll(teams);

        this.positionPlayers = BuildMocksFactory.buildPositionPlayers(teams);
        this.pitchers = BuildMocksFactory.buildPitchers(teams);

        this.positionPlayerRepository.saveAll(this.positionPlayers);
        this.pitcherRepository.saveAll(this.pitchers);

        this.positionPlayerDTOs = BuildMocksFactory.buildPositionPlayerDTOs();
        this.pitcherDTOs = BuildMocksFactory.buildPitcherDTOs();
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
            .containsOnly(PLAYER1_NAME);
    }

    @Test
    @DisplayName("Should return all pitchers")
    void testGetAllPitchers() {
        List<PitcherDTO> result = this.playerService.getAllPitchers();
        assertThat(result).hasSize(1).containsExactlyElementsOf(this.pitcherDTOs);
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
}