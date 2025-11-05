package com.mlb.mlbportal.unit.player;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mlb.mlbportal.repositories.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import com.mlb.mlbportal.dto.player.PitcherDTO;
import com.mlb.mlbportal.dto.player.PitcherSummaryDTO;
import com.mlb.mlbportal.dto.player.PlayerDTO;
import com.mlb.mlbportal.dto.player.PositionPlayerDTO;
import com.mlb.mlbportal.dto.player.PositionPlayerSummaryDTO;
import com.mlb.mlbportal.handler.notFound.PlayerNotFoundException;
import com.mlb.mlbportal.mappers.player.PitcherMapper;
import com.mlb.mlbportal.mappers.player.PositionPlayerMapper;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.PitcherPositions;
import com.mlb.mlbportal.models.enums.PlayerPositions;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
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

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {
    
    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PositionPlayerRepository positionPlayerRepository;

    @Mock
    private PitcherRepository pitcherRepository;

    @Mock
    private PositionPlayerMapper positionPlayerMapper;

    @Mock
    private PitcherMapper pitcherMapper;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private PlayerService playerService;

    private List<PositionPlayer> positionPlayers;
    private List<Pitcher> pitchers;
    private List<PositionPlayerDTO> positionPlayerDTOs;
    private List<PitcherDTO> pitcherDTOs;
    private List<PositionPlayerSummaryDTO> positionSummaryDTOs;
    private List<PitcherSummaryDTO> pitcherSummaryDTOs;
    private List<Team> teams;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.teams = BuildMocksFactory.setUpTeamMocks();

        this.positionPlayers = BuildMocksFactory.buildPositionPlayers(this.teams);
        this.pitchers = BuildMocksFactory.buildPitchers(this.teams);

        this.positionPlayerDTOs = BuildMocksFactory.buildPositionPlayerDTOs();
        this.pitcherDTOs = BuildMocksFactory.buildPitcherDTOs();
        this.positionSummaryDTOs = BuildMocksFactory.buildPositionPlayerSummaryDTOs();
        this.pitcherSummaryDTOs = BuildMocksFactory.buildPitcherSummaryDTOs();
    }

    @Test
    @DisplayName("Should return all players mapped and sorted")
    void testGetAllPlayers() {
        when(this.positionPlayerRepository.findAll()).thenReturn(this.positionPlayers);
        when(this.pitcherRepository.findAll()).thenReturn(this.pitchers);
        when(this.positionPlayerMapper.toListPositionPlayerDTO(this.positionPlayers)).thenReturn(this.positionPlayerDTOs);
        when(this.pitcherMapper.toListPitcherDTO(this.pitchers)).thenReturn(this.pitcherDTOs);

        List<PlayerDTO> result = this.playerService.getAllPlayers();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).name()).isEqualTo(PLAYER1_NAME);
        assertThat(result.get(0).teamName()).isEqualTo(this.teams.get(0).getName());
        assertThat(result.get(1).name()).isEqualTo(PLAYER2_NAME);
        assertThat(result.get(1).teamName()).isEqualTo(this.teams.get(0).getName());
        assertThat(result.get(2).name()).isEqualTo(PLAYER3_NAME);
        assertThat(result.get(2).teamName()).isEqualTo(this.teams.get(1).getName());
    }

    @Test
    @DisplayName("Should return all position players")
    void testGetAllPositionPlayers() {
        when(this.positionPlayerRepository.findAll()).thenReturn(this.positionPlayers);
        when(this.positionPlayerMapper.toListPositionPlayerDTO(this.positionPlayers)).thenReturn(this.positionPlayerDTOs);

        List<PositionPlayerDTO> result = this.playerService.getAllPositionPlayers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo(PLAYER1_NAME);
        assertThat(result.get(0).position()).isEqualTo(PlayerPositions.CF);
        assertThat(result.get(1).name()).isEqualTo(PLAYER2_NAME);
        assertThat(result.get(1).position()).isEqualTo(PlayerPositions.SS);
    }

    @Test
    @DisplayName("Should return all pitchers")
    void testGetAllPitchers() {
        when(this.pitcherRepository.findAll()).thenReturn(this.pitchers);
        when(this.pitcherMapper.toListPitcherDTO(this.pitchers)).thenReturn(this.pitcherDTOs);

        List<PitcherDTO> result = this.playerService.getAllPitchers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo(PLAYER3_NAME);
        assertThat(result.get(0).position()).isEqualTo(PitcherPositions.SP);
    }

    @Test
    @DisplayName("Should return the position player by its name")
    void testFindPositionPlayerByName() {
        PositionPlayer player = this.positionPlayers.get(0);
        when(this.playerRepository.findByName(PLAYER1_NAME)).thenReturn(Optional.of(player));
        when(this.positionPlayerMapper.toPositionPlayerDTO(player)).thenReturn(this.positionPlayerDTOs.get(0));

        assertThatNoException().isThrownBy(() -> this.playerService.findPlayerByName(PLAYER1_NAME));
        PlayerDTO result = this.playerService.findPlayerByName(player.getName());

        assertThat(result.name()).isEqualTo(player.getName());
    }

    @Test
    @DisplayName("Should return the pitcher by its name")
    void testFindPitcherByName() {
        Pitcher pitcher = this.pitchers.get(0);
        when(this.playerRepository.findByName(PLAYER3_NAME)).thenReturn(Optional.of(pitcher));
        when(this.pitcherMapper.toPitcherDTO(pitcher)).thenReturn(this.pitcherDTOs.get(0));

        assertThatNoException().isThrownBy(() -> this.playerService.findPlayerByName(PLAYER3_NAME));
        PlayerDTO result = this.playerService.findPlayerByName(pitcher.getName());

        assertThat(result.name()).isEqualTo(pitcher.getName());
    }

    @Test
    @DisplayName("Should throw PlayerNotFoundException for an unknown player")
    void testFindUnknownPlayerByName() {
        when(this.playerRepository.findByName(UNKNOWN_PLAYER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.playerService.findPlayerByName(UNKNOWN_PLAYER))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessageContaining("Player Not Found");
    }

    @Test
    @DisplayName("Should return updated position players of a known team")
    void testGetUpdatedPositionPlayersOfTeam() {
        Team team = this.teams.get(0);
        when(this.positionPlayerRepository.findByTeamOrderByNameAsc(team)).thenReturn(this.positionPlayers);

        List<PositionPlayer> result = this.playerService.getUpdatedPositionPlayersOfTeam(this.teams.get(0));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTeam().getName()).isEqualTo(TEST_TEAM1_NAME);
        assertThat(result.get(0).getName()).isEqualTo(PLAYER1_NAME);
    }

    @Test
    @DisplayName("Should return updated pitchers of a known team")
    void testGetUpdatedPitchersOfTeam() {
        Team team = this.teams.get(1);
        when(this.pitcherRepository.findByTeamOrderByNameAsc(team)).thenReturn(this.pitchers);

        List<Pitcher> result = this.playerService.getUpdatedPitchersOfTeam(this.teams.get(1));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTeam().getName()).isEqualTo(TEST_TEAM2_NAME);
        assertThat(result.get(0).getName()).isEqualTo(PLAYER3_NAME);
    }

    @Test
    @DisplayName("Should return paginated position players of a team")
    void testGetPositionPlayersOfTeam() {
        Team team = this.teams.get(0);
        when(this.teamRepository.findByName(team.getName())).thenReturn(Optional.of(team));
        when(this.positionPlayerRepository.findByTeamOrderByNameAsc(team)).thenReturn(this.positionPlayers);
        when(this.positionPlayerMapper.toPositionPlayerSummaryDTO(any())).thenReturn(this.positionSummaryDTOs.get(0), this.positionSummaryDTOs.get(1));

        Page<PositionPlayerSummaryDTO> result = this.playerService.getAllPositionPlayersOfATeam(TEST_TEAM1_NAME, 0, 10);
        List<PositionPlayerSummaryDTO> resultContent = result.getContent();

        assertThat(resultContent).hasSize(2);
        assertThat(resultContent.get(0).name()).isEqualTo(PLAYER1_NAME);
        assertThat(resultContent.get(1).name()).isEqualTo(PLAYER2_NAME);
        
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0); // page index
        assertThat(result.getSize()).isEqualTo(10);  // requested size
        assertThat(result.hasNext()).isFalse();
        assertThat(result.hasPrevious()).isFalse();
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

     @Test
    @DisplayName("Should return paginated pitchers of a team")
    void testGetPitchersOfTeam() {
        Team team = this.teams.get(1);
        when(this.teamRepository.findByName(team.getName())).thenReturn(Optional.of(team));
        when(this.pitcherRepository.findByTeamOrderByNameAsc(team)).thenReturn(pitchers);
        when(this.pitcherMapper.toPitcherSummaryDTO(any())).thenReturn(this.pitcherSummaryDTOs.get(0));

        Page<PitcherSummaryDTO> result = this.playerService.getAllPitchersOfATeam(TEST_TEAM2_NAME, 0, 10);
        List<PitcherSummaryDTO> resultContent = result.getContent();

        assertThat(resultContent).hasSize(1);
        assertThat(resultContent.get(0).name()).isEqualTo(PLAYER3_NAME);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0); // page index
        assertThat(result.getSize()).isEqualTo(10);  // requested size
        assertThat(result.hasNext()).isFalse();
        assertThat(result.hasPrevious()).isFalse();
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }
}