package com.mlb.mlbportal.unit.player;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.PitcherPositions;
import com.mlb.mlbportal.models.enums.PlayerPositions;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.player.PitcherRepository;
import com.mlb.mlbportal.repositories.player.PositionPlayerRepository;
import com.mlb.mlbportal.services.player.PlayerServiceOperations;
import com.mlb.mlbportal.utils.BuildMocksFactory;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.mlb.mlbportal.utils.TestConstants.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceOperationsTest {

    @Mock 
    private PositionPlayerRepository positionPlayerRepository;

    @Mock 
    private PitcherRepository pitcherRepository;

    private List<PositionPlayer> positionPlayers;
    private List<Pitcher> pitchers;
    private List<Team> teams;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        this.teams = BuildMocksFactory.setUpTeamMocks();
        this.positionPlayers = BuildMocksFactory.buildPositionPlayers(this.teams);
        this.pitchers = BuildMocksFactory.buildPitchers(this.teams);
    }

    @Test
    @DisplayName("Should calculate and update stats for position player and save if changed")
    void testUpdatePositionPlayerStats() {
        PositionPlayer player = this.positionPlayers.get(0);
        player.setAverage(0.0);
        player.setObp(0.0);
        player.setSlugging(0.0);
        player.setOps(0.0);

        PlayerServiceOperations.updatePlayerStats(player, this.positionPlayerRepository, this.pitcherRepository);

        double expectedAvg = truncate((double) PLAYER1_HITS / PLAYER1_AT_BATS);
        double expectedObp = truncate((double) (PLAYER1_HITS + PLAYER1_WALKS) / (PLAYER1_AT_BATS + PLAYER1_WALKS));
        int totalBases = PLAYER1_HITS + PLAYER1_DOUBLES + 2 * PLAYER1_TRIPLES + 3 * PLAYER1_HOME_RUNS;
        double expectedSlg = truncate((double) totalBases / PLAYER1_AT_BATS);
        double expectedOps = truncate(expectedObp + expectedSlg);

        assertThat(player.getAverage()).isEqualTo(expectedAvg);
        assertThat(player.getObp()).isEqualTo(expectedObp);
        assertThat(player.getSlugging()).isEqualTo(expectedSlg);
        assertThat(player.getOps()).isEqualTo(expectedOps);

        verify(this.positionPlayerRepository).save(player);
    }

    @Test
    @DisplayName("Should calculate and update stats for pitcher and save if changed")
    void testUpdatePitcherStats() {
        Pitcher pitcher = this.pitchers.get(0);
        pitcher.setEra(0.0);
        pitcher.setWhip(0.0);

        PlayerServiceOperations.updatePlayerStats(pitcher, this.positionPlayerRepository, this.pitcherRepository);

        double expectedEra = truncate((double) (PLAYER3_RUNS_ALLOWED * 9) / PLAYER3_INNINGS, 2);
        double expectedWhip = truncate((double) (PLAYER3_WALKS + PLAYER3_HITS_ALLOWED) / PLAYER3_INNINGS);

        assertThat(pitcher.getEra()).isEqualTo(expectedEra);
        assertThat(pitcher.getWhip()).isEqualTo(expectedWhip);

        verify(this.pitcherRepository).save(pitcher);
    }

    @Test
    @DisplayName("Should not save position player if stats remain unchanged")
    void testNoSaveIfPositionStatsUnchanged() {
        PositionPlayer player = this.positionPlayers.get(0);

        double avg = truncate((double) PLAYER1_HITS / PLAYER1_AT_BATS);
        double obp = truncate((double) (PLAYER1_HITS + PLAYER1_WALKS) / (PLAYER1_AT_BATS + PLAYER1_WALKS));
        int totalBases = PLAYER1_HITS + PLAYER1_DOUBLES + 2 * PLAYER1_TRIPLES + 3 * PLAYER1_HOME_RUNS;
        double slg = truncate((double) totalBases / PLAYER1_AT_BATS);
        double ops = truncate(obp + slg);

        player.setAverage(avg);
        player.setObp(obp);
        player.setSlugging(slg);
        player.setOps(ops);

        PlayerServiceOperations.updatePlayerStats(player, this.positionPlayerRepository, this.pitcherRepository);

        verify(this.positionPlayerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not save pitcher if stats remain unchanged")
    void testNoSaveIfPitcherStatsUnchanged() {
        Pitcher pitcher = this.pitchers.get(0);

        double era = truncate((double) (PLAYER3_RUNS_ALLOWED * 9) / PLAYER3_INNINGS, 2);
        double whip = truncate((double) (PLAYER3_WALKS + PLAYER3_HITS_ALLOWED) / PLAYER3_INNINGS);

        pitcher.setEra(era);
        pitcher.setWhip(whip);

        PlayerServiceOperations.updatePlayerStats(pitcher, this.positionPlayerRepository, this.pitcherRepository);

        verify(this.pitcherRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should assign 0.0 to AVG, OBP, SLG, OPS when atBats and walks are zero")
    void testZeroStatsForEmptyPositionPlayer() {
        PositionPlayer player = new PositionPlayer("EmptyPlayer", this.teams.get(0), PlayerPositions.CF, 0, 0, 0, 0, 0, 0, 0);

        PlayerServiceOperations.updatePlayerStats(player, this.positionPlayerRepository, this.pitcherRepository);

        assertThat(player.getAverage()).isEqualTo(0.0);
        assertThat(player.getObp()).isEqualTo(0.0);
        assertThat(player.getSlugging()).isEqualTo(0.0);
        assertThat(player.getOps()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should assign 0.0 to ERA and WHIP when innings pitched is zero")
    void testZeroStatsForEmptyPitcher() {
        Pitcher pitcher = new Pitcher("EmptyPitcher", this.teams.get(1), PitcherPositions.SP, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        PlayerServiceOperations.updatePlayerStats(pitcher, this.positionPlayerRepository, this.pitcherRepository);

        assertThat(pitcher.getEra()).isEqualTo(0.0);
        assertThat(pitcher.getWhip()).isEqualTo(0.0);
    }

    private double truncate(double value) {
        return ((int) (value * 1000)) / 1000.0;
    }

    private double truncate(double value, int decimals) {
        int factor = (int) Math.pow(10, decimals);
        return ((int) (value * factor)) / (double) factor;
    }
}