package com.mlb.mlbportal.unit.player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mlb.mlbportal.dto.player.CreatePlayerRequest;
import com.mlb.mlbportal.handler.badRequest.InvalidTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.mlb.mlbportal.dto.player.PlayerDTO;
import com.mlb.mlbportal.dto.player.pitcher.EditPitcherRequest;
import com.mlb.mlbportal.dto.player.pitcher.PitcherDTO;
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
import static com.mlb.mlbportal.utils.TestConstants.NEW_PLAYER_NAME;
import static com.mlb.mlbportal.utils.TestConstants.NEW_PLAYER_NUMBER;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_NUMBER;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_PLAYER;
import static com.mlb.mlbportal.utils.TestConstants.UNKNOWN_TEAM;

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

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideCreateCases")
    void testCreatePlayer(String description, String type, CreatePlayerRequest<?> request, PlayerDTO expectedDto,
                          boolean shouldFail, Class<? extends Throwable> expectedException, String expectedMessage) {

        if (description.contains("Duplicate")) {
            when(this.playerRepository.findByName(anyString())).thenReturn(Optional.of(new PositionPlayer()));
        }
        else {
            when(this.playerRepository.findByName(anyString())).thenReturn(Optional.empty());
            if (description.contains("Team Not Found")) {
                when(this.teamRepository.findByNameOrThrow(anyString())).thenThrow(new TeamNotFoundException());
            }
            else {
                Team team = new Team();
                team.setName(request.teamName());

                if (description.contains("Roster Full")) {
                    team.setPositionPlayers(new ArrayList<>(Arrays.asList(new PositionPlayer[12])));
                    team.setPitchers(new ArrayList<>(Arrays.asList(new Pitcher[12])));
                }
                when(this.teamRepository.findByNameOrThrow(anyString())).thenReturn(team);
            }
        }

        if (shouldFail) {
            assertThatThrownBy(() -> this.playerService.createPlayer(type, request))
                    .isInstanceOf(expectedException);
        }
        else {
            if (request.position() instanceof PlayerPositions) {
                when(this.positionPlayerMapper.toPositionPlayerDTO(any())).thenReturn((PositionPlayerDTO) expectedDto);
            }
            else {
                when(this.pitcherMapper.toPitcherDTO(any())).thenReturn((PitcherDTO) expectedDto);
            }

            PlayerDTO result = this.playerService.createPlayer(type, request);

            assertThat(result.name()).isEqualTo(NEW_PLAYER_NAME);
            verify(this.playerRepository, times(1)).save(any());
        }
    }

    private static Stream<Arguments> provideCreateCases() {
        String teamName = TEST_TEAM1_NAME;

        CreatePlayerRequest<PlayerPositions> posReq = new CreatePlayerRequest<>(
                NEW_PLAYER_NAME, NEW_PLAYER_NUMBER, teamName, PlayerPositions.CF);

        CreatePlayerRequest<PitcherPositions> pitReq = new CreatePlayerRequest<>(
                NEW_PLAYER_NAME, NEW_PLAYER_NUMBER, teamName, PitcherPositions.SP);

        PositionPlayerDTO posDto = new PositionPlayerDTO(
                NEW_PLAYER_NAME, NEW_PLAYER_NUMBER, teamName, PlayerPositions.CF,
                0,0,0,0,0,0,0,0,0,0,0,null, false);

        PitcherDTO pitDto = new PitcherDTO(
                NEW_PLAYER_NAME, NEW_PLAYER_NUMBER, teamName, PitcherPositions.SP,
                0,0,0,0,0,0,0,0,0,0,0,0,null, false);

        return Stream.of(
                Arguments.of("Success - Position Player", "position", posReq, posDto, false, null, null),
                Arguments.of("Success - Pitcher", "pitcher", pitReq, pitDto, false, null, null),

                Arguments.of("Error - Duplicate Player", "position", posReq, null, true, PlayerAlreadyExistsException.class, "Player Already Exists"),
                Arguments.of("Error - Team Not Found", "position", new CreatePlayerRequest<>(NEW_PLAYER_NAME, 1, UNKNOWN_TEAM, PlayerPositions.CF), null, true, TeamNotFoundException.class, "Team Not Found"),
                Arguments.of("Error - Roster Full", "position", posReq, null, true, RosterFullException.class, teamName + " roster is full"),

                Arguments.of("Error - Invalid Position for Type", "pitcher", posReq, null, true, InvalidTypeException.class, "Position must be a pitching position")
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
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
        );
        EditPitcherRequest pitcherRequest = new EditPitcherRequest(
                Optional.empty(), Optional.of(NEW_PLAYER_NUMBER), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
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
                0,0,0,0,0,0,0,0,0,0,0,null, true
        );

        Pitcher pitcher = new Pitcher();
        pitcher.setName(PLAYER1_NAME);
        PitcherDTO pitDto = new PitcherDTO(
                pitcher.getName(), PLAYER1_NUMBER, TEST_TEAM1_NAME, PitcherPositions.SP,
                0,0,0,0,0,0,0,0,0,0,0,0,null, true
        );

        return Stream.of(
                Arguments.of("position player", posPlayer, posDto, PLAYER1_NAME, false),
                Arguments.of("pitcher", pitcher, pitDto, PLAYER1_NAME, false),
                Arguments.of("unknown player", null, null, UNKNOWN_PLAYER, true)
        );
    }
}