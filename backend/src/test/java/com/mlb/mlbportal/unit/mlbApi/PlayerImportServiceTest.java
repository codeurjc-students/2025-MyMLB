package com.mlb.mlbportal.unit.mlbApi;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
}