package com.mlb.mlbportal.unit.player;

import com.mlb.mlbportal.dto.player.PlayerDTO;
import com.mlb.mlbportal.dto.player.pitcher.CreatePitcherRequest;
import com.mlb.mlbportal.dto.player.pitcher.EditPitcherRequest;
import com.mlb.mlbportal.dto.player.pitcher.PitcherDTO;
import com.mlb.mlbportal.dto.player.position.CreatePositionPlayerRequest;
import com.mlb.mlbportal.dto.player.position.EditPositionPlayerRequest;
import com.mlb.mlbportal.dto.player.position.PositionPlayerDTO;
import com.mlb.mlbportal.handler.conflict.PlayerAlreadyExistsException;
import com.mlb.mlbportal.handler.conflict.RosterFullException;
import com.mlb.mlbportal.handler.notFound.PlayerNotFoundException;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.mappers.player.PitcherMapper;
import com.mlb.mlbportal.mappers.player.PositionPlayerMapper;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.PitcherPositions;
import com.mlb.mlbportal.models.enums.PlayerPositions;
import com.mlb.mlbportal.models.others.PictureInfo;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.Player;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.player.PitcherRepository;
import com.mlb.mlbportal.repositories.player.PlayerRepository;
import com.mlb.mlbportal.repositories.player.PositionPlayerRepository;
import com.mlb.mlbportal.services.player.PlayerService;
import com.mlb.mlbportal.services.uploader.PictureService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.mlb.mlbportal.utils.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceWriteOperationsTest {
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

    @InjectMocks
    private PlayerService playerService;

    private List<PositionPlayer> positionPlayers;

    @BeforeEach
    void setUp() {
        List<Team> teams = BuildMocksFactory.setUpTeamMocks();
        this.positionPlayers = BuildMocksFactory.buildPositionPlayers(teams);
    }

    @ParameterizedTest(name = "Create player case: {0}")
    @MethodSource("provideCreateCases")
    void testCreatePlayer(String type, Object request, Object dto, Team team,
                          boolean shouldFail, Class<? extends Throwable> expectedException, String expectedMessage) {
        if (shouldFail) {
            if (expectedException == PlayerAlreadyExistsException.class) {
                when(this.playerRepository.findByName(anyString())).thenReturn(Optional.of(this.positionPlayers.getFirst()));
            }
            else if (expectedException == TeamNotFoundException.class) {
                when(this.playerRepository.findByName(anyString())).thenReturn(Optional.empty());
                when(this.teamRepository.findByNameOrThrow(UNKNOWN_TEAM)).thenCallRealMethod();
            }
            else if (expectedException == RosterFullException.class) {
                team.setPositionPlayers(IntStream.range(0, 24).mapToObj(i -> new PositionPlayer()).toList());
                when(this.playerRepository.findByName(anyString())).thenReturn(Optional.empty());
                when(this.teamRepository.findByNameOrThrow(team.getName())).thenReturn(team);
            }

            assertThatThrownBy(() -> {
                if (type.equals("pitcher")) {
                    this.playerService.createPitcher((CreatePitcherRequest) request);
                } else {
                    this.playerService.createPositionPlayer((CreatePositionPlayerRequest) request);
                }
            })
                    .isInstanceOf(expectedException)
                    .hasMessageContaining(expectedMessage);
        }
        else {
            when(this.playerRepository.findByName(anyString())).thenReturn(Optional.empty());
            when(this.teamRepository.findByNameOrThrow(team.getName())).thenReturn(team);

            if (request instanceof CreatePositionPlayerRequest posReq && dto instanceof PositionPlayerDTO posDto) {
                when(this.positionPlayerMapper.toPositionPlayerDTO(any(PositionPlayer.class))).thenReturn(posDto);
                PositionPlayerDTO result = this.playerService.createPositionPlayer(posReq);
                assertThat(result.name()).isEqualTo(NEW_PLAYER_NAME);
            }
            else if (request instanceof CreatePitcherRequest pitReq && dto instanceof PitcherDTO pitDto) {
                when(this.pitcherMapper.toPitcherDTO(any(Pitcher.class))).thenReturn(pitDto);
                PitcherDTO result = this.playerService.createPitcher(pitReq);
                assertThat(result.name()).isEqualTo(NEW_PLAYER_NAME);
            }
        }
    }

    private static Stream<Arguments> provideCreateCases() {
        Team team = new Team();
        team.setName(TEST_TEAM1_NAME);

        PositionPlayerDTO posDto = new PositionPlayerDTO(
                NEW_PLAYER_NAME, NEW_PLAYER_NUMBER, team.getName(), PlayerPositions.CF,
                0,0,0,0,0,0,0,0,0,0,0,null
        );
        CreatePositionPlayerRequest posReq = new CreatePositionPlayerRequest(
                NEW_PLAYER_NAME, NEW_PLAYER_NUMBER, team.getName(), PlayerPositions.CF
        );

        PitcherDTO pitDto = new PitcherDTO(
                NEW_PLAYER_NAME, NEW_PLAYER_NUMBER, team.getName(), PitcherPositions.RP,
                0,0,0,0,0,0,0,0,0,0,0,0,null
        );
        CreatePitcherRequest pitReq = new CreatePitcherRequest(
                NEW_PLAYER_NAME, NEW_PLAYER_NUMBER, team.getName(), PitcherPositions.SP
        );

        return Stream.of(
                Arguments.of("position player", posReq, posDto, team, false, null, null),
                Arguments.of("pitcher", pitReq, pitDto, team, false, null, null),
                Arguments.of("duplicate player", posReq, null, team, true, PlayerAlreadyExistsException.class, "Player Already Exists"),
                Arguments.of("team not found", new CreatePositionPlayerRequest(NEW_PLAYER_NAME, 1, UNKNOWN_TEAM, PlayerPositions.CF), null, team, true, TeamNotFoundException.class, "Team Not Found"),
                Arguments.of("roster full", posReq, null, team, true, RosterFullException.class, team.getName() + " roster is full")
        );
    }

    @ParameterizedTest(name = "Update player case: {0}")
    @MethodSource("providePlayersAndRequestsIncludingFailure")
    void testUpdatePlayer(String type, Player player, Object request, String playerName, boolean shouldFail) {
        if (shouldFail) {
            if (request instanceof EditPositionPlayerRequest posReq) {
                when(this.positionPlayerRepository.findByNameOrThrow(playerName)).thenCallRealMethod();

                assertThatThrownBy(() -> this.playerService.updatePositionPlayer(playerName, posReq))
                        .isInstanceOf(PlayerNotFoundException.class)
                        .hasMessageContaining("Player Not Found");
            }
            else if (request instanceof EditPitcherRequest pitReq) {
                when(this.pitcherRepository.findByNameOrThrow(playerName)).thenCallRealMethod();

                assertThatThrownBy(() -> this.playerService.updatePitcher(playerName, pitReq))
                        .isInstanceOf(PlayerNotFoundException.class)
                        .hasMessageContaining("Player Not Found");
            }
        }
        else {
            // Success Cases
            if (player instanceof PositionPlayer pp && request instanceof EditPositionPlayerRequest req) {
                when(this.positionPlayerRepository.findByNameOrThrow(pp.getName())).thenReturn(pp);
                this.playerService.updatePositionPlayer(pp.getName(), req);
                assertThat(pp.getPlayerNumber()).isEqualTo(NEW_PLAYER_NUMBER);
            }
            else if (player instanceof Pitcher p && request instanceof EditPitcherRequest req) {
                when(this.pitcherRepository.findByNameOrThrow(p.getName())).thenReturn(p);
                this.playerService.updatePitcher(p.getName(), req);
                assertThat(p.getPlayerNumber()).isEqualTo(NEW_PLAYER_NUMBER);
            }
            verify(this.playerRepository, times(1)).save(any(Player.class));
        }
    }

    private static Stream<Arguments> providePlayersAndRequestsIncludingFailure() {
        PositionPlayer positionPlayer = new PositionPlayer();
        positionPlayer.setName(PLAYER1_NAME);
        Pitcher pitcher = new Pitcher();
        pitcher.setName(PLAYER1_NAME);

        EditPositionPlayerRequest positionRequest = new EditPositionPlayerRequest(
                Optional.empty(), Optional.of(NEW_PLAYER_NUMBER), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
        );
        EditPitcherRequest pitcherRequest = new EditPitcherRequest(
                Optional.empty(), Optional.of(NEW_PLAYER_NUMBER), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
        );

        return Stream.of(
                Arguments.of("position player", positionPlayer, positionRequest, PLAYER1_NAME, false),
                Arguments.of("pitcher", pitcher, pitcherRequest, PLAYER1_NAME, false),
                Arguments.of("unknown player", null, positionRequest, UNKNOWN_PLAYER, true)
        );
    }

    @Test
    @DisplayName("Should upload and update the player's picture")
    void testUploadPicture() throws IOException {
        PositionPlayer player = this.positionPlayers.getFirst();
        MultipartFile mockFile = mock(MultipartFile.class);
        PictureInfo pictureInfo = new PictureInfo("http://cloudinary.com/test123.jpg", "test123");

        when(this.playerRepository.findByNameOrThrow(player.getName())).thenReturn(player);
        when(this.pictureService.uploadPicture(mockFile)).thenReturn(pictureInfo);

        PictureInfo result = this.playerService.updatePicture(player.getName(), mockFile);
        assertThat(result).isNotNull();
        verify(this.playerRepository).save(player);
    }

    @ParameterizedTest(name = "Delete player case: {0}")
    @MethodSource("providePlayersAndDTOsIncludingFailure")
    void testDeletePlayer(String type, Player player, PlayerDTO dto, String playerName, boolean shouldFail) {
        if (shouldFail) {
            when(this.playerRepository.findByNameOrThrow(playerName)).thenCallRealMethod();
            assertThatThrownBy(() -> this.playerService.deletePlayer(playerName))
                    .isInstanceOf(PlayerNotFoundException.class)
                    .hasMessageContaining("Player Not Found");
        }
        else {
            Team mockTeam = new Team();
            mockTeam.setName(TEST_TEAM1_NAME);
            player.setTeam(mockTeam);

            when(this.playerRepository.findByNameOrThrow(player.getName())).thenReturn(player);
            when(this.teamRepository.findByNameOrThrow(TEST_TEAM1_NAME)).thenReturn(mockTeam);

            if (player instanceof PositionPlayer pp && dto instanceof PositionPlayerDTO posDto) {
                when(this.positionPlayerMapper.toPositionPlayerDTO(pp)).thenReturn(posDto);
            }
            else if (player instanceof Pitcher p && dto instanceof PitcherDTO pitDto) {
                when(this.pitcherMapper.toPitcherDTO(p)).thenReturn(pitDto);
            }

            PlayerDTO result = this.playerService.deletePlayer(player.getName());

            assertThat(result.name()).isEqualTo(player.getName());
            verify(this.playerRepository).delete(player);
        }
    }

    private static Stream<Arguments> providePlayersAndDTOsIncludingFailure() {
        PositionPlayer posPlayer = new PositionPlayer();
        posPlayer.setName(PLAYER1_NAME);
        PositionPlayerDTO posDto = new PositionPlayerDTO(
                posPlayer.getName(), PLAYER1_NUMBER, TEST_TEAM1_NAME, PlayerPositions.CF,
                0,0,0,0,0,0,0,0,0,0,0,null
        );

        Pitcher pitcher = new Pitcher();
        pitcher.setName(PLAYER1_NAME);
        PitcherDTO pitDto = new PitcherDTO(
                pitcher.getName(), PLAYER1_NUMBER, TEST_TEAM1_NAME, PitcherPositions.SP,
                0,0,0,0,0,0,0,0,0,0,0,0,null
        );

        return Stream.of(
                Arguments.of("position player", posPlayer, posDto, PLAYER1_NAME, false),
                Arguments.of("pitcher", pitcher, pitDto, PLAYER1_NAME, false),
                Arguments.of("unknown player", null, null, UNKNOWN_PLAYER, true)
        );
    }
}