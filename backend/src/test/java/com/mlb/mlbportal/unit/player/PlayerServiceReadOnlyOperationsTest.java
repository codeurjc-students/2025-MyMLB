package com.mlb.mlbportal.unit.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_PLAYER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mlb.mlbportal.dto.player.PlayerRankingsDTO;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.services.uploader.PictureService;
import com.mlb.mlbportal.services.utilities.PaginationHandlerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import com.mlb.mlbportal.dto.player.PlayerDTO;
import com.mlb.mlbportal.dto.player.pitcher.PitcherDTO;
import com.mlb.mlbportal.dto.player.pitcher.PitcherSummaryDTO;
import com.mlb.mlbportal.dto.player.position.PositionPlayerDTO;
import com.mlb.mlbportal.dto.player.position.PositionPlayerSummaryDTO;
import com.mlb.mlbportal.handler.notFound.PlayerNotFoundException;
import com.mlb.mlbportal.mappers.player.PitcherMapper;
import com.mlb.mlbportal.mappers.player.PositionPlayerMapper;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.player.PitcherRepository;
import com.mlb.mlbportal.repositories.player.PlayerRepository;
import com.mlb.mlbportal.repositories.player.PositionPlayerRepository;
import com.mlb.mlbportal.services.player.PlayerService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PlayerServiceReadOnlyOperationsTest {

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

    @Mock
    private PictureService pictureService;

    @Mock
    private PaginationHandlerService paginationHandlerService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<PlayerRankingsDTO> dataQuery;

    @Mock
    private TypedQuery<Long> countQuery;

    @InjectMocks
    private PlayerService playerService;

    private List<PositionPlayer> positionPlayers;
    private List<Pitcher> pitchers;
    private List<PositionPlayerDTO> positionPlayerDTOs;
    private List<PitcherDTO> pitcherDTOs;
    private List<PlayerDTO> allPlayers;
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

        this.allPlayers = new ArrayList<>();
        this.allPlayers.addAll(positionPlayerDTOs);
        this.allPlayers.addAll(pitcherDTOs);
        this.allPlayers.sort((p1, p2) -> p1.name().compareToIgnoreCase(p2.name()));

        ReflectionTestUtils.setField(this.playerService, "self", this.playerService);
        ReflectionTestUtils.setField(this.playerService, "entityManager", this.entityManager);
    }

    @Test
    @DisplayName("Should return all players mapped and sorted")
    void testGetAllPlayers() {
        Page<PlayerDTO> mockPage = new PageImpl<>(this.allPlayers, PageRequest.of(0, 10), this.allPlayers.size());
        when(this.positionPlayerRepository.findAll()).thenReturn(this.positionPlayers);
        when(this.pitcherRepository.findAll()).thenReturn(this.pitchers);
        doReturn(mockPage).when(this.paginationHandlerService).paginateAndMap(anyList(), eq(0), eq(10), any());

        Page<PlayerDTO> result = this.playerService.getAllPlayers(0, 10);

        assertThat(result.getContent()).hasSize(allPlayers.size()).containsExactlyElementsOf(allPlayers);
        assertThat(result.getTotalElements()).isEqualTo(allPlayers.size());
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Should return all position players")
    void testGetAllPositionPlayers() {
        Page<PositionPlayer> mockPage = new PageImpl<>(this.positionPlayers, PageRequest.of(0, 10), this.positionPlayers.size());
        when(this.positionPlayerRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(this.positionPlayerMapper.toPositionPlayerDTO(this.positionPlayers.getFirst())).thenReturn(this.positionPlayerDTOs.getFirst());
        when(this.positionPlayerMapper.toPositionPlayerDTO(this.positionPlayers.get(1))).thenReturn(this.positionPlayerDTOs.get(1));

        Page<PositionPlayerDTO> result = this.playerService.getAllPositionPlayers(0, 10);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactlyElementsOf(this.positionPlayerDTOs);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should return all pitchers")
    void testGetAllPitchers() {
        Page<Pitcher> mockPage = new PageImpl<>(this.pitchers, PageRequest.of(0, 10), this.pitchers.size());

        when(this.pitcherRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(this.pitcherMapper.toPitcherDTO(this.pitchers.getFirst())).thenReturn(this.pitcherDTOs.getFirst());

        Page<PitcherDTO> result = this.playerService.getAllPitchers(0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).containsExactlyElementsOf(this.pitcherDTOs);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should return the position player by its name")
    void testFindPositionPlayerByName() {
        PositionPlayer player = this.positionPlayers.getFirst();
        when(this.playerRepository.findByNameOrThrow(PLAYER1_NAME)).thenReturn(player);
        when(this.positionPlayerMapper.toPositionPlayerDTO(player)).thenReturn(this.positionPlayerDTOs.getFirst());

        assertThatNoException().isThrownBy(() -> this.playerService.findPlayerByName(PLAYER1_NAME));
        PlayerDTO result = this.playerService.findPlayerByName(player.getName());

        assertThat(result.name()).isEqualTo(player.getName());
    }

    @Test
    @DisplayName("Should return the pitcher by its name")
    void testFindPitcherByName() {
        Pitcher pitcher = this.pitchers.getFirst();
        when(this.playerRepository.findByNameOrThrow(PLAYER3_NAME)).thenReturn(pitcher);
        when(this.pitcherMapper.toPitcherDTO(pitcher)).thenReturn(this.pitcherDTOs.getFirst());

        assertThatNoException().isThrownBy(() -> this.playerService.findPlayerByName(PLAYER3_NAME));
        PlayerDTO result = this.playerService.findPlayerByName(pitcher.getName());

        assertThat(result.name()).isEqualTo(pitcher.getName());
    }

    @Test
    @DisplayName("Should throw PlayerNotFoundException for an unknown player")
    void testFindUnknownPlayerByName() {
        when(this.playerRepository.findByNameOrThrow(UNKNOWN_PLAYER)).thenCallRealMethod();

        assertThatThrownBy(() -> this.playerService.findPlayerByName(UNKNOWN_PLAYER))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessageContaining("Player Not Found");
    }

    @Test
    @DisplayName("Should return paginated position players of a team")
    void testGetPositionPlayersOfTeam() {
        Team team = this.teams.getFirst();
        Page<PositionPlayerSummaryDTO> mockPage = new PageImpl<>(this.positionSummaryDTOs, PageRequest.of(0, 10), this.positionSummaryDTOs.size());

        when(this.teamRepository.findByNameOrThrow(team.getName())).thenReturn(team);
        when(this.positionPlayerRepository.findByTeamOrderByNameAsc(team)).thenReturn(this.positionPlayers);
        doReturn(mockPage).when(this.paginationHandlerService).paginateAndMap(eq(this.positionPlayers), eq(0), eq(10), any());

        Page<PositionPlayerSummaryDTO> result = this.playerService.getAllPositionPlayersOfATeam(TEST_TEAM1_NAME, 0, 10);
        List<PositionPlayerSummaryDTO> resultContent = result.getContent();

        assertThat(resultContent).hasSize(this.positionSummaryDTOs.size());
        assertThat(resultContent.get(0).name()).isEqualTo(PLAYER1_NAME);
        assertThat(resultContent.get(1).name()).isEqualTo(PLAYER2_NAME);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should return paginated pitchers of a team")
    void testGetPitchersOfTeam() {
        Team team = this.teams.get(1);
        Page<PitcherSummaryDTO> mockPage = new PageImpl<>(this.pitcherSummaryDTOs, PageRequest.of(0, 10), this.pitcherSummaryDTOs.size());

        when(this.teamRepository.findByNameOrThrow(team.getName())).thenReturn(team);
        when(this.pitcherRepository.findByTeamOrderByNameAsc(team)).thenReturn(this.pitchers);
        doReturn(mockPage).when(this.paginationHandlerService).paginateAndMap(eq(this.pitchers), eq(0), eq(10), any());

        Page<PitcherSummaryDTO> result = this.playerService.getAllPitchersOfATeam(TEST_TEAM2_NAME, 0, 10);
        List<PitcherSummaryDTO> resultContent = result.getContent();

        assertThat(resultContent).hasSize(1);
        assertThat(resultContent.getFirst().name()).isEqualTo(PLAYER3_NAME);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should return top players ranking of a given stat")
    void testGetTopPlayersRanking() {
        PlayerRankingsDTO rankingDTO = new PlayerRankingsDTO(PLAYER1_NAME, "https://picture", 0.300);
        List<PlayerRankingsDTO> content = List.of(rankingDTO);

        when(this.entityManager.createQuery(anyString(), eq(PlayerRankingsDTO.class))).thenReturn(this.dataQuery);
        when(this.entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(this.countQuery);
        when(this.dataQuery.getResultList()).thenReturn(content);
        when(this.countQuery.getSingleResult()).thenReturn(1L);

        Page<PlayerRankingsDTO> result = this.playerService.getTopPlayersRanking(
                0, 10, "position", "average", List.of(TEST_TEAM1_NAME), null, null
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo(PLAYER1_NAME);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(this.dataQuery).setFirstResult(0);
        verify(this.dataQuery).setMaxResults(10);
        verify(this.entityManager, times(2)).createQuery(anyString(), any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid stat")
    void testGetTopPlayersRankingInvalidStat() {
        assertThatThrownBy(() -> this.playerService.getTopPlayersRanking(0, 10, "position", "stat", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The provided stat is not valid");
    }

    @Test
    @DisplayName("Should return all stats rankings")
    void testGetAllStatsRankings() {
        PlayerRankingsDTO rankingDTO = new PlayerRankingsDTO(PLAYER1_NAME, "https://picture", 0.300);
        List<PlayerRankingsDTO> content = List.of(rankingDTO);

        when(this.entityManager.createQuery(anyString(), eq(PlayerRankingsDTO.class))).thenReturn(this.dataQuery);
        when(this.entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(this.countQuery);
        when(this.dataQuery.getResultList()).thenReturn(content);
        when(this.countQuery.getSingleResult()).thenReturn(1L);

        Map<String, List<PlayerRankingsDTO>> result = playerService.getAllStatsRankings("position", null, null, null);

        assertThat(result).isNotEmpty().containsKey("average");
        assertThat(result.get("average")).extracting("name").contains(PLAYER1_NAME);
        verify(entityManager, atLeastOnce()).createQuery(anyString(), eq(PlayerRankingsDTO.class));
    }
}