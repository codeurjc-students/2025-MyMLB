package com.mlb.mlbportal.unit.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_PLAYER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mlb.mlbportal.dto.player.PlayerRankingsDTO;
import com.mlb.mlbportal.handler.badRequest.InvalidTypeException;
import com.mlb.mlbportal.repositories.TeamRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import org.springframework.data.domain.Sort;
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
    private List<Team> teams;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.teams = BuildMocksFactory.setUpTeamMocks();

        this.positionPlayers = BuildMocksFactory.buildPositionPlayers(this.teams);
        this.pitchers = BuildMocksFactory.buildPitchers(this.teams);

        this.positionPlayerDTOs = BuildMocksFactory.buildPositionPlayerDTOs();
        this.pitcherDTOs = BuildMocksFactory.buildPitcherDTOs();
        List<PositionPlayerSummaryDTO> positionSummaryDTOs = BuildMocksFactory.buildPositionPlayerSummaryDTOs();
        List<PitcherSummaryDTO> pitcherSummaryDTOs = BuildMocksFactory.buildPitcherSummaryDTOs();

        List<PlayerDTO> allPlayers = new ArrayList<>();
        allPlayers.addAll(positionPlayerDTOs);
        allPlayers.addAll(pitcherDTOs);
        allPlayers.sort((p1, p2) -> p1.name().compareToIgnoreCase(p2.name()));

        ReflectionTestUtils.setField(this.playerService, "self", this.playerService);
        ReflectionTestUtils.setField(this.playerService, "entityManager", this.entityManager);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideGetAllPlayersScenarios")
    @DisplayName("Should return paginated players based on filters")
    void testGetAllPlayersParametrized(String description, String type, String name, String team, int expectedSize) {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));

        if (name != null) {
            PositionPlayer player = this.positionPlayers.getFirst();
            when(this.playerRepository.findByNameOrThrow(name)).thenReturn(player);
            when(this.positionPlayerMapper.toPositionPlayerDTO(player)).thenReturn(this.positionPlayerDTOs.getFirst());
        }
        else if (type != null) {
            if ("position".equals(type)) {
                Page<PositionPlayer> page = new PageImpl<>(this.positionPlayers, pageable, this.positionPlayers.size());
                if (team != null) {
                    when(this.teamRepository.findByNameOrThrow(team)).thenReturn(this.teams.getFirst());
                    when(this.positionPlayerRepository.findByTeamOrderByNameAsc(any(), any())).thenReturn(page);
                }
                else {
                    when(this.positionPlayerRepository.findAll(any(Pageable.class))).thenReturn(page);
                }
                when(this.positionPlayerMapper.toPositionPlayerDTO(any())).thenReturn(this.positionPlayerDTOs.getFirst());
            }
            else {
                Page<Pitcher> page = new PageImpl<>(this.pitchers, pageable, this.pitchers.size());
                when(this.pitcherRepository.findAll(any(Pageable.class))).thenReturn(page);
                when(this.pitcherMapper.toPitcherDTO(any())).thenReturn(this.pitcherDTOs.getFirst());
            }
        }
        else {
            Page<PositionPlayer> page = new PageImpl<>(this.positionPlayers, pageable, this.positionPlayers.size());
            if (team != null) {
                when(this.teamRepository.findByNameOrThrow(team)).thenReturn(this.teams.getFirst());
                when(this.playerRepository.findByTeamOrderByNameAsc(any(), any())).thenAnswer(i -> page);
            }
            else {
                when(this.playerRepository.findAll(any(Pageable.class))).thenAnswer(i -> page);
            }
            when(this.positionPlayerMapper.toPositionPlayerDTO(any())).thenReturn(this.positionPlayerDTOs.getFirst());
        }

        Page<PlayerDTO> result = this.playerService.getAllPlayers(type, name, team, 0, 10);

        assertThat(result.getContent()).hasSize(expectedSize);
    }

    private static Stream<Arguments> provideGetAllPlayersScenarios() {
        return Stream.of(
                Arguments.of("Filter by Name", null, PLAYER1_NAME, null, 1),
                Arguments.of("Filter by Type (Position)", "position", null, null, 2),
                Arguments.of("Filter by Type (Pitcher)", "pitcher", null, null, 1),
                Arguments.of("Filter by Team (No type)", null, null, TEST_TEAM1_NAME, 2),
                Arguments.of("Filter by Type and Team", "position", null, TEST_TEAM1_NAME, 2),
                Arguments.of("All Players (No filters)", null, null, null, 2)
        );
    }

    @Test
    @DisplayName("Should throw PlayerNotFoundException for an unknown player")
    void testFindUnknownPlayerByName() {
        when(this.playerRepository.findByNameOrThrow(UNKNOWN_PLAYER)).thenThrow(new PlayerNotFoundException());

        assertThatThrownBy(() -> this.playerService.getAllPlayers(null, UNKNOWN_PLAYER, null, 0, 10))
                .isInstanceOf(PlayerNotFoundException.class)
                .hasMessage("Player Not Found");
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
    @DisplayName("Should throw InvalidTypeException for invalid stat")
    void testGetTopPlayersRankingInvalidStat() {
        assertThatThrownBy(() -> this.playerService.getTopPlayersRanking(0, 10, "position", "stat", null, null, null))
                .isInstanceOf(InvalidTypeException.class)
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