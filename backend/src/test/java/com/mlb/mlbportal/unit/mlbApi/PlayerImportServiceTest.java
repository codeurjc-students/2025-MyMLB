package com.mlb.mlbportal.unit.mlbApi;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_NAME;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.mlb.mlbportal.dto.mlbapi.player.PlayerData;
import com.mlb.mlbportal.dto.mlbapi.player.PlayerDetailInfo;
import com.mlb.mlbportal.dto.mlbapi.player.PlayerResponse;
import com.mlb.mlbportal.dto.mlbapi.player.PositionData;
import com.mlb.mlbportal.dto.mlbapi.player.RosterEntry;
import com.mlb.mlbportal.dto.mlbapi.player.RosterResponse;
import com.mlb.mlbportal.dto.mlbapi.player.Split;
import com.mlb.mlbportal.dto.mlbapi.player.StatData;
import com.mlb.mlbportal.dto.mlbapi.player.Stats;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.player.PitcherRepository;
import com.mlb.mlbportal.repositories.player.PositionPlayerRepository;
import com.mlb.mlbportal.services.mlbAPI.PlayerImportService;
import com.mlb.mlbportal.utils.BuildMocksFactory;

@ExtendWith(MockitoExtension.class)
class PlayerImportServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PitcherRepository pitcherRepository;

    @Mock
    private PositionPlayerRepository positionPlayerRepository;

    @InjectMocks
    private PlayerImportService playerImportService;

    private List<Team> mockTeams;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        var field = PlayerImportService.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(this.playerImportService, this.restTemplate);
        this.mockTeams = BuildMocksFactory.setUpTeamMocks();
    }

    @Test
    @DisplayName("Should obtain the players for each team and their stats from Stats API successfully")
    void testGetTeamRoster() {
        when(this.teamRepository.findAll()).thenReturn(List.of(this.mockTeams.getFirst()));

        PlayerData person = new PlayerData(10, "Mike Trout");
        PositionData positionData = new PositionData("8", "CF");
        RosterEntry entry = new RosterEntry(person, positionData);
        RosterResponse rosterResponse = new RosterResponse(List.of(entry));

        when(this.restTemplate.getForObject(anyString(), eq(RosterResponse.class))).thenReturn(rosterResponse);

        PlayerResponse playerResponse = getPlayerResponse();

        when(this.restTemplate.getForObject(contains("/people/"), eq(PlayerResponse.class))).thenReturn(playerResponse);

        when(this.positionPlayerRepository.findByName(anyString())).thenReturn(Optional.empty());

        this.playerImportService.getTeamRoster();

        verify(this.positionPlayerRepository, times(1)).save(any(PositionPlayer.class));
        verify(this.pitcherRepository, never()).save(any(Pitcher.class));
    }

    private static PlayerResponse getPlayerResponse() {
        StatData statData = new StatData(250, 50, 20, 10, 15, 6, 1, 0.250, 0.250, 1.110, 0.990, 0, 0, 0, 0, 0, 0, 0, 0.00, 0.00, "0.0");
        Split split = new Split(statData);
        Stats stats = new Stats(List.of(split));

        PositionData position = new PositionData("8", "CF");
        PlayerDetailInfo playerDetail = new PlayerDetailInfo(10, "Mike Trout", "27", position, List.of(stats));
        return new PlayerResponse(List.of(playerDetail));
    }

    @Test
    @DisplayName("Should call the fallbackGetTeamsRoster method when API fails")
    void testGetTeamRosterFallback() {
        assertDoesNotThrow(() -> {
            this.playerImportService.fallbackGetTeamsRoster(new RuntimeException("Stats API Down"));
        });
    }

    @Test
    @DisplayName("Should save a Position Player")
    void testSavePositionPlayer() {
        when(this.teamRepository.findAll()).thenReturn(List.of(this.mockTeams.getFirst()));

        PlayerData playerData = new PlayerData(20, PLAYER2_NAME);
        PositionData positionData = new PositionData("2", "C");
        RosterEntry entry = new RosterEntry(playerData, positionData);
        RosterResponse rosterResponse = new RosterResponse(List.of(entry));

        when(this.restTemplate.getForObject(anyString(), eq(RosterResponse.class))).thenReturn(rosterResponse);

        StatData positionPlayerStats = new StatData(20, 10, 2, 2, 6, 0, 0, 0.500, 0.500, 1.554, 0.999,
                0, 0, 0, 0, 0, 0, 0, 0.00, 0.00, "0.0");
        Split split = new Split(positionPlayerStats);
        Stats stats = new Stats(List.of(split));
        PlayerDetailInfo playerDetailInfo = new PlayerDetailInfo(20, PLAYER2_NAME, "3", positionData, List.of(stats));
        PlayerResponse playerResponse = new PlayerResponse(List.of(playerDetailInfo));

        when(this.restTemplate.getForObject(contains("/people/"), eq(PlayerResponse.class))).thenReturn(playerResponse);
        when(this.positionPlayerRepository.findByName(anyString())).thenReturn(Optional.empty());

        this.playerImportService.getTeamRoster();

        verify(this.positionPlayerRepository, times(1)).save(any(PositionPlayer.class));
    }

    @Test
    @DisplayName("Should save a Pitcher")
    void testSavePitcher() {
        when(this.teamRepository.findAll()).thenReturn(List.of(this.mockTeams.getFirst()));

        PlayerData playerData = new PlayerData(19, PLAYER1_NAME);
        PositionData positionData = new PositionData("1", "P");
        RosterEntry entry = new RosterEntry(playerData, positionData);
        RosterResponse rosterResponse = new RosterResponse(List.of(entry));

        when(this.restTemplate.getForObject(anyString(), eq(RosterResponse.class))).thenReturn(rosterResponse);

        StatData pitcherStats = new StatData(0, 0, 0, 0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0,
                10, 3, 5, 2, 80, 10, 5, 4.00, 1.10, "50.2");
        Split split = new Split(pitcherStats);
        Stats stats = new Stats(List.of(split));
        PlayerDetailInfo playerDetailInfo = new PlayerDetailInfo(19, PLAYER1_NAME, "17", positionData, List.of(stats));
        PlayerResponse playerResponse = new PlayerResponse(List.of(playerDetailInfo));

        when(this.restTemplate.getForObject(contains("/people/"), eq(PlayerResponse.class))).thenReturn(playerResponse);
        when(this.pitcherRepository.findByName(anyString())).thenReturn(Optional.empty());

        this.playerImportService.getTeamRoster();

        verify(this.pitcherRepository, times(1)).save(any(Pitcher.class));
    }

    @Test
    @DisplayName("Should handle empty API response gracefully without saving any player")
    void testGetTeamRosterEmptyResponse() {
        when(this.teamRepository.findAll()).thenReturn(List.of(this.mockTeams.getFirst()));
        when(this.restTemplate.getForObject(anyString(), eq(RosterResponse.class))).thenReturn(null);

        this.playerImportService.getTeamRoster();

        verify(this.positionPlayerRepository, never()).save(any());
        verify(this.pitcherRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should set a default picture when the fallback method has been called")
    void testFallbackGetPlayerPicture() {
        var result = this.playerImportService.fallbackGetPlayerPicture(999, new RuntimeException("Timeout"));

        assertThat(result).isNotNull();
        assertThat(result.getUrl()).contains("generic/headshot");
    }

    @Test
    @DisplayName("Should handle invalid position label")
    void testManageInvalidPlayerPosition() {
        when(this.teamRepository.findAll()).thenReturn(List.of(this.mockTeams.getFirst()));

        PositionData invalidPos = new PositionData("99", "CH");
        RosterResponse rosterResponse = new RosterResponse(List.of(new RosterEntry(new PlayerData(1, PLAYER1_NAME), invalidPos)));

        when(this.restTemplate.getForObject(anyString(), eq(RosterResponse.class))).thenReturn(rosterResponse);

        PlayerDetailInfo playerDetail = new PlayerDetailInfo(1, PLAYER1_NAME, "1", invalidPos, List.of());
        when(this.restTemplate.getForObject(contains("/people/"), eq(PlayerResponse.class))).thenReturn(new PlayerResponse(List.of(playerDetail)));

        assertDoesNotThrow(() -> this.playerImportService.getTeamRoster());
    }
}