package com.mlb.mlbportal.integration.player;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import com.mlb.mlbportal.dto.player.PlayerDTO;
import com.mlb.mlbportal.dto.player.position.CreatePositionPlayerRequest;
import com.mlb.mlbportal.dto.player.position.EditPositionPlayerRequest;
import com.mlb.mlbportal.dto.player.position.PositionPlayerDTO;
import com.mlb.mlbportal.handler.notFound.PlayerNotFoundException;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.PlayerPositions;
import com.mlb.mlbportal.models.others.PictureInfo;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.player.PitcherRepository;
import com.mlb.mlbportal.repositories.player.PlayerRepository;
import com.mlb.mlbportal.repositories.player.PositionPlayerRepository;
import com.mlb.mlbportal.services.player.PlayerService;
import com.mlb.mlbportal.utils.BuildMocksFactory;
import static com.mlb.mlbportal.utils.TestConstants.NEW_PLAYER_NAME;
import static com.mlb.mlbportal.utils.TestConstants.NEW_PLAYER_NUMBER;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlayerServiceWriteOperationsIntegrationTest {
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
        List<com.mlb.mlbportal.models.player.Pitcher> pitchers = BuildMocksFactory.buildPitchers(teams);

        this.positionPlayerRepository.saveAll(positionPlayers);
        this.pitcherRepository.saveAll(pitchers);
    }

    @Test
    @DisplayName("Should create the player and correctly associated with the team")
    void testPlayerCreation() {
        CreatePositionPlayerRequest request =
                new CreatePositionPlayerRequest(NEW_PLAYER_NAME, NEW_PLAYER_NUMBER, TEST_TEAM1_NAME, PlayerPositions.SS);

        PositionPlayerDTO player = this.playerService.createPositionPlayer(request);

        assertThat(player).isNotNull();
        assertThat(player.name()).isEqualTo(NEW_PLAYER_NAME);

        Team team = this.teamRepository.findByName(TEST_TEAM1_NAME).orElseThrow();

        assertThat(team.getPositionPlayers())
                .extracting(PositionPlayer::getName)
                .contains(player.name());
    }

    @Test
    @DisplayName("Should update the player stats and team correctly")
    void testUpdatePlayer() {
        EditPositionPlayerRequest request = new EditPositionPlayerRequest(
                Optional.of(TEST_TEAM2_NAME),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        this.playerService.updatePositionPlayer(PLAYER1_NAME, request);

        PositionPlayer updated = this.positionPlayerRepository.findByName(PLAYER1_NAME).orElseThrow();
        assertThat(updated.getTeam().getName()).isEqualTo(TEST_TEAM2_NAME);

        Team newTeam = this.teamRepository.findByName(TEST_TEAM2_NAME).orElseThrow();

        assertThat(newTeam.getPositionPlayers())
                .extracting(PositionPlayer::getName)
                .contains(PLAYER1_NAME);
    }

    @Test
    @DisplayName("Should upload picture and persist URL + publicId")
    void testUpdatePicture() throws Exception {
        PositionPlayer player = this.positionPlayerRepository.findByName(PLAYER1_NAME).orElseThrow(PlayerNotFoundException::new);

        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "fake".getBytes());
        PictureInfo dto = this.playerService.updatePicture(PLAYER1_NAME, file);

        assertThat(dto.getUrl()).contains("http://fake.cloudinary.com/test.jpg");
        assertThat(dto.getPublicId()).isEqualTo("fake123");
        assertThat(player.getPicture().getUrl()).isEqualTo("http://fake.cloudinary.com/test.jpg");
        assertThat(player.getPicture().getPublicId()).isEqualTo("fake123");
    }

    @Test
    @DisplayName("Should delete the player and remove association from the team")
    void testDeletePlayer() {
        PlayerDTO deleted = this.playerService.deletePlayer(PLAYER1_NAME);

        assertThat(deleted).isNotNull();
        assertThat(deleted.name()).isEqualTo(PLAYER1_NAME);

        assertThat(this.playerRepository.findByName(PLAYER1_NAME)).isEmpty();

        Team team = this.teamRepository.findByName(TEST_TEAM1_NAME).orElseThrow();
        assertThat(team.getPositionPlayers())
                .extracting(PositionPlayer::getName)
                .doesNotContain(PLAYER1_NAME);
    }
}